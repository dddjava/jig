package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDescription;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotation;

import java.util.List;

/**
 * メソッド
 */
public class Method {

    MethodDeclaration methodDeclaration;
    DecisionNumber decisionNumber;
    List<MethodAnnotation> methodAnnotations;

    public Method(MethodDeclaration methodDeclaration, DecisionNumber decisionNumber, List<MethodAnnotation> methodAnnotations) {
        this.methodDeclaration = methodDeclaration;
        this.decisionNumber = decisionNumber;
        this.methodAnnotations = methodAnnotations;
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
        return methodAnnotations.stream()
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
        return methodAnnotations.stream()
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
                .map(MethodAnnotation::description)
                .orElseGet(AnnotationDescription::new);
    }

    public List<MethodAnnotation> methodAnnotations() {
        return methodAnnotations;
    }
}
