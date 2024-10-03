package org.dddjava.jig.report;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSourceReadService;
import org.dddjava.jig.domain.model.models.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
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
    void readProjectData(JigService jigService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        ServiceAngles serviceAngles = jigService.serviceAngles();

        assertThat(serviceAngles.list().stream().map(ServiceReport::new))
                .extracting(
                        serviceReport -> serviceReport.method().declaration().declaringType(),
                        serviceReport -> serviceReport.method().declaration().asSignatureSimpleText(),
                        serviceReport -> serviceReport.method().declaration().methodReturn().typeIdentifier(),
                        serviceReport -> serviceReport.usingFromController(),
                        serviceReport -> serviceReport.usingRepositoryMethods()
                ).contains(
                tuple(
                        new TypeIdentifier(CanonicalService.class),
                        "fuga(FugaIdentifier)",
                        new TypeIdentifier(Fuga.class),
                        false,
                        "[FugaRepository.get(FugaIdentifier), HogeRepository.method()]"
                ),
                tuple(
                        new TypeIdentifier(DecisionService.class),
                        "分岐のあるメソッド(Object)",
                        new TypeIdentifier("void"),
                        false,
                        "[]"
                ),
                tuple(
                        new TypeIdentifier(SimpleService.class),
                        "RESTコントローラーから呼ばれる()",
                        new TypeIdentifier("void"),
                        true,
                        "[]"
                ),
                tuple(
                        new TypeIdentifier(SimpleService.class),
                        "コントローラーから呼ばれない()",
                        new TypeIdentifier("void"),
                        false,
                        "[]"
                ),
                tuple(
                        new TypeIdentifier(SimpleService.class),
                        "コントローラーから呼ばれる()",
                        new TypeIdentifier("void"),
                        true,
                        "[]"
                )
        );
    }
}
