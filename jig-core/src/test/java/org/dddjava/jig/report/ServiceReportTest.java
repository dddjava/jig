package org.dddjava.jig.report;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.models.applications.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.infrastructure.view.report.application.ServiceReport;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.application.service.DecisionService;
import stub.application.service.SimpleService;
import stub.domain.model.type.fuga.Fuga;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class ServiceReportTest {

    @Test
    void readProjectData(JigService jigService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        ServiceAngles serviceAngles = jigService.serviceAngles(jigSource);

        assertThat(serviceAngles.list().stream().map(ServiceReport::new))
                .extracting(
                        serviceReport -> serviceReport.method().declaration().declaringType(),
                        serviceReport -> serviceReport.method().declaration().asSignatureSimpleText(),
                        serviceReport -> serviceReport.method().declaration().methodReturn().typeIdentifier(),
                        serviceReport -> serviceReport.usingFromController(),
                        serviceReport -> serviceReport.usingRepositoryMethods()
                ).contains(
                        tuple(
                                TypeIdentifier.from(CanonicalService.class),
                                "fuga(FugaIdentifier)",
                                TypeIdentifier.from(Fuga.class),
                                false,
                                "[FugaRepository.get(FugaIdentifier), HogeRepository.method()]"
                        ),
                        tuple(
                                TypeIdentifier.from(DecisionService.class),
                                "分岐のあるメソッド(Object)",
                                TypeIdentifier.valueOf("void"),
                                false,
                                "[]"
                        ),
                        tuple(
                                TypeIdentifier.from(SimpleService.class),
                                "RESTコントローラーから呼ばれる()",
                                TypeIdentifier.valueOf("void"),
                                true,
                                "[]"
                        ),
                        tuple(
                                TypeIdentifier.from(SimpleService.class),
                                "コントローラーから呼ばれない()",
                                TypeIdentifier.valueOf("void"),
                                false,
                                "[]"
                        ),
                        tuple(
                                TypeIdentifier.from(SimpleService.class),
                                "コントローラーから呼ばれる()",
                                TypeIdentifier.valueOf("void"),
                                true,
                                "[]"
                        )
                );
    }
}
