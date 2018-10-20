package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.services.ServiceAngle;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.application.service.DecisionService;
import stub.application.service.SimpleService;
import stub.domain.model.type.fuga.Fuga;
import testing.JigServiceTest;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JigServiceTest
public class ServiceAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService, ApplicationService applicationService) {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        TypeByteCodes typeByteCodes = implementationService.readProjectData(localProject);

        ServiceAngles serviceAngles = applicationService.serviceAngles(typeByteCodes);

        assertThat(serviceAngles.list())
                .extracting(
                        serviceAngle -> serviceAngle.method().declaringType(),
                        serviceAngle -> serviceAngle.method().asSignatureSimpleText(),
                        serviceAngle -> serviceAngle.method().methodReturn().typeIdentifier(),
                        ServiceAngle::usingFromController,
                        serviceAngle -> serviceAngle.usingRepositoryMethods()
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
