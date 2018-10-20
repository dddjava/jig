package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.collections.CollectionAngle;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.values.ValueAngle;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.*;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaName;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValueAngleTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService implementationService = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader()
    );

    BusinessRuleService service = new BusinessRuleService(new BusinessRuleCondition(".+\\.domain\\.model\\..+"));

    @Test
    void readProjectData() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        ValueAngles identifiers = service.values(ValueKind.IDENTIFIER, projectData);
        assertThat(identifiers.list()).extracting(ValueAngle::typeIdentifier).contains(
                new TypeIdentifier(SimpleIdentifier.class),
                new TypeIdentifier(FugaIdentifier.class),
                new TypeIdentifier(FugaName.class)
        );

        ValueAngles numbers = service.values(ValueKind.NUMBER, projectData);
        assertThat(numbers.list()).extracting(ValueAngle::typeIdentifier).contains(
                new TypeIdentifier(SimpleNumber.class)
        );

        ValueAngles dates = service.values(ValueKind.DATE, projectData);
        assertThat(dates.list()).extracting(ValueAngle::typeIdentifier).contains(
                new TypeIdentifier(SimpleDate.class)
        );

        ValueAngles terms = service.values(ValueKind.TERM, projectData);
        assertThat(terms.list()).extracting(ValueAngle::typeIdentifier).contains(
                new TypeIdentifier(SimpleTerm.class)
        );

        CollectionAngles collections = service.collections(projectData);
        assertThat(collections.list()).extracting(CollectionAngle::typeIdentifier).contains(
                new TypeIdentifier(SimpleCollection.class),
                new TypeIdentifier(SetCollection.class)
        );
    }
}
