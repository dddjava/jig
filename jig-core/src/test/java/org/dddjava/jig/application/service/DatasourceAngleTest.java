package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceAngles;
import org.dddjava.jig.domain.model.jigsource.file.Sources;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaRepository;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class DatasourceAngleTest {

    @Test
    void readProjectData(ApplicationService applicationService, Sources sources, ImplementationService implementationService) {
        implementationService.readProjectData(sources);
        implementationService.readSql(sources.sqlSources());

        DatasourceAngles datasourceAngles = applicationService.datasourceAngles();

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
