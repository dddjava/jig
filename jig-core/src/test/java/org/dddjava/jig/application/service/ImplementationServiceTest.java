package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.basic.ClassFindFailException;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.domain.model.values.ValueTypes;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.*;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaName;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImplementationServiceTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService sut = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader(),
            new PropertyCharacterizedTypeFactory()
    );

    @Test
    void 対象ソースなし() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[0]);
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);

        assertThatThrownBy(() -> sut.readProjectData(localProject))
                .isInstanceOf(ClassFindFailException.class);
    }

    @Test
    void readProjectData() throws URISyntaxException {
        // テストクラスのルートを取得する
        URI location = ImplementationServiceTest.class.getProtectionDomain().getCodeSource().getLocation().toURI();

        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(location)});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = sut.readProjectData(localProject);

        // 期待する 値 が取得できていること
        ValueTypes valueTypes = projectData.valueTypes();

        assertThat(valueTypes.extract(ValueKind.IDENTIFIER)).matches(typeIdentifiers ->
                typeIdentifiers.contains(new TypeIdentifier(SimpleIdentifier.class))
                        && typeIdentifiers.contains(new TypeIdentifier(FugaIdentifier.class))
                        && typeIdentifiers.contains(new TypeIdentifier(FugaName.class)));

        assertThat(valueTypes.extract(ValueKind.NUMBER)).matches(typeIdentifiers ->
                typeIdentifiers.contains(new TypeIdentifier(SimpleNumber.class)));

        assertThat(valueTypes.extract(ValueKind.DATE)).matches(typeIdentifiers ->
                typeIdentifiers.contains(new TypeIdentifier(SimpleDate.class)));

        assertThat(valueTypes.extract(ValueKind.TERM)).matches(typeIdentifiers ->
                typeIdentifiers.contains(new TypeIdentifier(SimpleTerm.class)));

        assertThat(valueTypes.extract(ValueKind.COLLECTION)).matches(typeIdentifiers ->
                typeIdentifiers.contains(new TypeIdentifier(SimpleCollection.class))
                        && typeIdentifiers.contains(new TypeIdentifier(SetCollection.class)));
    }
}
