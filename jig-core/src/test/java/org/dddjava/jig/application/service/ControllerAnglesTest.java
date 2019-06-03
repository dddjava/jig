package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class ControllerAnglesTest {

    @Test
    void readProjectData(ApplicationService applicationService, AnalyzedImplementation analyzedImplementation) {
        ControllerAngles angles = applicationService.controllerAngles(analyzedImplementation);

        assertThat(angles.list())
                .extracting(
                        angle -> angle.method().declaration().asFullNameText(),
                        angle -> new ControllerReport(angle, "dummy").path()
                )
                .containsExactlyInAnyOrder(
                        tuple("stub.presentation.controller.SimpleController.getService()", "simple-class/simple-method"),
                        tuple("stub.presentation.controller.SimpleRestController.getService()", "test-get")
                );
    }
}
