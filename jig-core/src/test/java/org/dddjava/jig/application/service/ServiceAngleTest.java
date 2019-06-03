package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.application.service.DecisionService;
import stub.application.service.SimpleService;
import stub.domain.model.type.fuga.Fuga;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class ServiceAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService, ApplicationService applicationService, RawSource source) {
        TypeByteCodes typeByteCodes = implementationService.readProjectData(source);

        ServiceAngles serviceAngles = applicationService.serviceAngles(new AnalyzedImplementation(null, typeByteCodes, null));

        assertThat(serviceAngles.list())
                .extracting(
                        serviceAngle -> serviceAngle.method().declaringType(),
                        serviceAngle -> serviceAngle.method().asSignatureSimpleText(),
                        serviceAngle -> serviceAngle.method().methodReturn().typeIdentifier(),
                        ServiceAngle::usingFromController,
                        serviceAngle -> serviceAngle.usingRepositoryMethods().asSimpleText()
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
