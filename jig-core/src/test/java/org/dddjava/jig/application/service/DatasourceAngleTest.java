package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.threelayer.datasources.DatasourceAngles;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaRepository;
import testing.JigServiceTest;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JigServiceTest
public class DatasourceAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService, ApplicationService applicationService) {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        TypeByteCodes typeByteCodes = implementationService.readProjectData(localProject.createSource());
        Sqls sqls = implementationService.readSql(localProject.getSqlSources());

        DatasourceAngles datasourceAngles = applicationService.datasourceAngles(typeByteCodes, sqls);

        assertThat(datasourceAngles.list())
                .extracting(
                        datasourceAngle -> datasourceAngle.method().declaringType(),
                        datasourceAngle -> datasourceAngle.method().asSignatureSimpleText(),
                        datasourceAngle -> datasourceAngle.method().methodReturn().typeIdentifier(),
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
