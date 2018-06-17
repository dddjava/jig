package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.Layer;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DecisionAngleTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService implementationService = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader(),
            new PropertyCharacterizedTypeFactory()
    );

    AngleService angleService = new AngleService();

    @Test
    void readProjectData() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        DecisionAngles decisionAngles = angleService.decision(projectData);

        assertThat(decisionAngles.filter(Layer.APPLICATION))
                .extracting(decisionAngle -> decisionAngle.method().declaration().asFullNameText())
                .contains("stub.application.service.DecisionService.分岐のあるメソッド(java.lang.Object)");

        assertThat(decisionAngles.filter(Layer.DATASOURCE))
                .extracting(decisionAngle -> decisionAngle.method().declaration().asFullNameText())
                .contains("stub.infrastructure.datasource.DecisionDatasource.分岐のあるメソッド(java.lang.Object)");

        assertThat(decisionAngles.filter(Layer.PRESENTATION))
                .extracting(decisionAngle -> decisionAngle.method().declaration().asFullNameText())
                .contains("stub.presentation.controller.DecisionController.分岐のあるメソッド(java.lang.Object)");
    }
}
