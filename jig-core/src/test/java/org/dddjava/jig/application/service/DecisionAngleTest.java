package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.raw.raw.RawSource;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;

@JigServiceTest
public class DecisionAngleTest {

    @Test
    void readProjectData(ImplementationService implementationService, ApplicationService applicationService, RawSource source) {
        TypeByteCodes typeByteCodes = implementationService.readProjectData(source);

        DecisionAngles decisionAngles = applicationService.decision(typeByteCodes);

        assertThat(decisionAngles.listApplications())
                .extracting(decisionAngle -> decisionAngle.method().declaration().asFullNameText())
                .contains("stub.application.service.DecisionService.分岐のあるメソッド(java.lang.Object)");

        assertThat(decisionAngles.listInfrastructures())
                .extracting(decisionAngle -> decisionAngle.method().declaration().asFullNameText())
                .contains("stub.infrastructure.datasource.DecisionDatasource.分岐のあるメソッド(java.lang.Object)");

        assertThat(decisionAngles.listPresentations())
                .extracting(decisionAngle -> decisionAngle.method().declaration().asFullNameText())
                .contains("stub.presentation.controller.DecisionController.分岐のあるメソッド(java.lang.Object)");
    }
}
