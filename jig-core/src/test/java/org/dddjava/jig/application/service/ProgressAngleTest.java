package org.dddjava.jig.application.service;

import org.assertj.core.api.SoftAssertions;
import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.application.service.DecisionService;
import stub.application.service.SimpleService;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.presentation.controller.DecisionController;
import stub.presentation.controller.SimpleController;
import stub.presentation.controller.SimpleRestController;
import testing.TestSupport;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProgressAngleTest {

    // テストのためにSpringを起動したくないので自分でインスタンス生成する
    ImplementationService implementationService = new ImplementationService(
            new AsmByteCodeFactory(),
            new GlossaryService(
                    new JavaparserJapaneseReader(),
                    new OnMemoryJapaneseNameRepository()
            ),
            new MyBatisSqlReader()
    );

    ApplicationService applicationService = new ApplicationService(new Architecture(new BusinessRuleCondition(".+")));

    @Test
    void readProjectData() {
        Layout layoutMock = mock(Layout.class);
        when(layoutMock.extractClassPath()).thenReturn(new Path[]{Paths.get(TestSupport.defaultPackageClassURI())});
        when(layoutMock.extractSourcePath()).thenReturn(new Path[0]);

        LocalProject localProject = new LocalProject(layoutMock);
        ProjectData projectData = implementationService.readProjectData(localProject);

        ProgressAngles sut = applicationService.progressAngles(projectData);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(
                sut.progressOf(methodOf(DecisionController.class, "分岐のあるメソッド", singletonList(Object.class)))
        ).isEqualTo("DecisionControllerのクラスに付けた進捗");
        softly.assertThat(
                sut.progressOf(methodOf(SimpleController.class, "getService", emptyList()))
        ).isEqualTo("SimpleController#getServiceの進捗");
        softly.assertThat(
                sut.progressOf(methodOf(SimpleRestController.class, "getService", emptyList()))
        ).isEqualTo("");
        softly.assertThat(
                sut.progressOf(methodOf(CanonicalService.class, "fuga", singletonList(FugaIdentifier.class)))
        ).isEqualTo("CanonicalServiceクラスに付けた進捗");
        softly.assertThat(
                sut.progressOf(methodOf(CanonicalService.class, "method", emptyList()))
        ).isEqualTo("CanonicalServiceクラスに付けた進捗");
        softly.assertThat(
                sut.progressOf(methodOf(DecisionService.class, "分岐のあるメソッド", singletonList(Object.class)))
        ).isEqualTo("DecisionService#分岐のあるメソッドの進捗");
        softly.assertThat(
                sut.progressOf(methodOf(SimpleService.class, "コントローラーから呼ばれる", emptyList()))
        ).isEqualTo("SimpleService#コントローラーから呼ばれるの進捗");
        softly.assertThat(
                sut.progressOf(methodOf(SimpleService.class, "RESTコントローラーから呼ばれる", emptyList()))
        ).isEqualTo("RESTコントローラーから呼ばれるの進捗");
        softly.assertThat(
                sut.progressOf(methodOf(SimpleService.class, "コントローラーから呼ばれない", emptyList()))
        ).isEqualTo("SimpleServiceのクラスに付けた進捗");
    }

    private MethodDeclaration methodOf(Class<?> type, String name, List<Class<?>> arguments) {
        return new MethodDeclaration(
                new TypeIdentifier(type),
                new MethodSignature(name, new Arguments(
                        arguments.stream().map(TypeIdentifier::new).collect(Collectors.toList())
                )),
                new MethodReturn(new TypeIdentifier("void"))
        );
    }
}
