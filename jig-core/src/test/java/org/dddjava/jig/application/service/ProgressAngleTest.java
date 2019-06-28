package org.dddjava.jig.application.service;

import org.assertj.core.api.SoftAssertions;
import org.dddjava.jig.domain.model.declaration.method.Arguments;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.fact.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.junit.jupiter.api.Test;
import stub.application.service.CanonicalService;
import stub.application.service.DecisionService;
import stub.application.service.SimpleService;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.presentation.controller.DecisionController;
import stub.presentation.controller.SimpleController;
import stub.presentation.controller.SimpleRestController;
import testing.JigServiceTest;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

@JigServiceTest
public class ProgressAngleTest {

    @Test
    void readProjectData(ApplicationService applicationService, AnalyzedImplementation analyzedImplementation) {
        ProgressAngles sut = applicationService.progressAngles(analyzedImplementation);

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
