package org.dddjava.jig.report;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.infrastructure.view.report.application.RepositoryReport;
import org.junit.jupiter.api.Test;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaRepository;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class RepositoryReportTest {

    @Test
    void readProjectData(JigService jigService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        jigSource.addSqls(jigSourceReader.readSqlSource(sources.sqlSources()));

        DatasourceAngles datasourceAngles = jigService.datasourceAngles(jigSource);

        assertThat(datasourceAngles.list().stream().map(RepositoryReport::new))
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
