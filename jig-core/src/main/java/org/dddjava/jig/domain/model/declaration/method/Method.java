package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethod;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDescription;

import java.util.List;

/**
 * メソッド
 */
public class Method {

    MethodDeclaration methodDeclaration;
    DecisionNumber decisionNumber;
    List<AnnotatedMethod> annotatedMethods;

    public Method(MethodDeclaration methodDeclaration, DecisionNumber decisionNumber, List<AnnotatedMethod> annotatedMethods) {
        this.methodDeclaration = methodDeclaration;
        this.decisionNumber = decisionNumber;
        this.annotatedMethods = annotatedMethods;
    }

    public MethodDeclaration declaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return decisionNumber;
    }

    public boolean hasDecision() {
        return decisionNumber.notZero();
    }

    public boolean isControllerMethod(CharacterizedTypes characterizedTypes) {
        return annotatedMethods.stream()
                .anyMatch(annotatedMethod -> {
                            String annotationName = annotatedMethod.annotationType().fullQualifiedName();
                            // RequestMappingをメタアノテーションとして使うものにしたいが、spring-webに依存させたくないので列挙にする
                            // そのため独自アノテーションに対応できない
                            return annotationName.equals("org.springframework.web.bind.annotation.RequestMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.GetMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.PostMapping");
                        }
                )
                && characterizedTypes.stream().pickup(methodDeclaration.declaringType()).has(Characteristic.CONTROLLER);
    }

    public AnnotationDescription requestMappingDescription() {
        return annotatedMethods.stream()
                // WET
                .filter(annotatedMethod -> {
                            String annotationName = annotatedMethod.annotationType().fullQualifiedName();
                            // RequestMappingをメタアノテーションとして使うものにしたいが、spring-webに依存させたくないので列挙にする
                            // そのため独自アノテーションに対応できない
                            return annotationName.equals("org.springframework.web.bind.annotation.RequestMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.GetMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.PostMapping");
                        }
                )
                .findFirst()
                .map(AnnotatedMethod::description)
                .orElseGet(AnnotationDescription::new);
    }
}
