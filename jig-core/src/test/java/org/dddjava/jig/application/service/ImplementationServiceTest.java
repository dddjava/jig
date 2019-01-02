package org.dddjava.jig.application.service;

import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.junit.jupiter.api.Test;
import testing.JigServiceTest;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JigServiceTest
public class ImplementationServiceTest {

    @Test
    void 対象ソースなし(ImplementationService implementationService) {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[0]);
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject();

        assertThatThrownBy(() -> implementationService.readProjectData(localProject.createSource(layoutMock)))
                .isInstanceOf(ClassFindFailException.class);
    }
}
