package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.raw.RawSource;
import org.dddjava.jig.domain.model.threelayer.datasources.DatasourceAngles;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaRepository;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class DatasourceAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService, ApplicationService applicationService, RawSource source) {
        TypeByteCodes typeByteCodes = implementationService.readProjectData(source);
        Sqls sqls = implementationService.readSql(source.sqlSources());

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
