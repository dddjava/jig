package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaRepository;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatasourceAngleTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService implementationService = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader(),
            new PropertyCharacterizedTypeFactory()
    );

    AngleService angleService = new AngleService();

    @Test
    void readProjectData() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        DatasourceAngles datasourceAngles = angleService.datasourceAngles(projectData);

        assertThat(datasourceAngles.list())
                .extracting(
                        datasourceAngle -> datasourceAngle.method().declaringType(),
                        datasourceAngle -> datasourceAngle.method().asSignatureSimpleText(),
                        datasourceAngle -> datasourceAngle.method().returnType(),
                        datasourceAngle -> datasourceAngle.insertTables(),
                        datasourceAngle -> datasourceAngle.selectTables(),
                        datasourceAngle -> datasourceAngle.updateTables(),
                        datasourceAngle -> datasourceAngle.deleteTables()
                ).contains(
                tuple(
                        new TypeIdentifier(FugaRepository.class),
                        "get(FugaIdentifier)",
                        new TypeIdentifier(Fuga.class),
                        "[sut.piyo]", "[fuga]", "[]", "[]"
                ),
                tuple(
                        new TypeIdentifier(FugaRepository.class),
                        "register(Fuga)",
                        new TypeIdentifier("void"),
                        "[]", "[]", "[]", "[]"
                )
        );
    }
}
