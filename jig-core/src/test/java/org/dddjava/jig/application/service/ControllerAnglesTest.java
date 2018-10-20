package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JigServiceTest
public class ControllerAnglesTest {

    @Test
    void readProjectData(ImplementationService implementationService, ApplicationService applicationService) {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        TypeByteCodes typeByteCodes = implementationService.readProjectData(localProject);

        ControllerAngles angles = applicationService.controllerAngles(typeByteCodes);

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
