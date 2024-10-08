package org.dddjava.jig.report;

import org.dddjava.jig.application.JigService;
import org.dddjava.jig.application.JigSourceReader;
import org.dddjava.jig.domain.model.sources.file.Sources;
import org.dddjava.jig.infrastructure.view.report.application.ControllerReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JigServiceTest
public class ControllerReportTest {

    @Test
    void test(JigService jigService, Sources sources, JigSourceReader jigSourceReader) {
        var jigSource = jigSourceReader.readProjectData(sources);
        var entrypoint = jigService.entrypoint(jigSource);

        assertThat(entrypoint.listRequestHandlerMethods().stream().map(ControllerReport::new))
                .extracting(
                        controllerReport -> controllerReport.method().declaration().asFullNameText(),
                        controllerReport -> controllerReport.path()
                )
                .containsExactlyInAnyOrder(
                        tuple("stub.presentation.controller.SimpleController#getService()", "simple-class/simple-method"),
                        tuple("stub.presentation.controller.SimpleRestController#getService()", "test-get"),
                        tuple("stub.presentation.controller.SimpleRestController#swaggerOperationAnnotated()", "swagger-operation-annotated"),
                        tuple("stub.presentation.controller.SimpleRestController#swaggerOperationAnnotatedNoneSummary()", "swagger-operation-annotated-none-summary")
                );
    }
}
