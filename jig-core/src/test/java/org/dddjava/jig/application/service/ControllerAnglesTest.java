package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class ControllerAnglesTest {

    @Test
    void readProjectData(ApplicationService applicationService, AnalyzedImplementation analyzedImplementation) {
        ControllerMethods controllerMethods = applicationService.controllerAngles(analyzedImplementation);

        assertThat(controllerMethods.list())
                .extracting(
                        controllerMethod -> controllerMethod.method().declaration().asFullNameText(),
                        controllerMethod -> new ControllerReport(controllerMethod).path()
                )
                .containsExactlyInAnyOrder(
                        tuple("stub.presentation.controller.SimpleController.getService()", "simple-class/simple-method"),
                        tuple("stub.presentation.controller.SimpleRestController.getService()", "test-get")
                );
    }
}
