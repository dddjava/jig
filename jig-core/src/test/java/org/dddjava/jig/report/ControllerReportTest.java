package org.dddjava.jig.report;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.JigSourceReadService;
import org.dddjava.jig.domain.model.models.presentations.ControllerMethods;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class ControllerReportTest {

    @Test
    void test(ApplicationService applicationService, Sources sources, JigSourceReadService jigSourceReadService) {
        jigSourceReadService.readProjectData(sources);
        ControllerMethods controllerMethods = applicationService.controllerAngles();

        assertThat(controllerMethods.list().stream().map(ControllerReport::new))
                .extracting(
                        controllerReport -> controllerReport.method().declaration().asFullNameText(),
                        controllerReport -> controllerReport.path()
                )
                .containsExactlyInAnyOrder(
                        tuple("stub.presentation.controller.SimpleController.getService()", "simple-class/simple-method"),
                        tuple("stub.presentation.controller.SimpleRestController.getService()", "test-get")
                );
    }
}
