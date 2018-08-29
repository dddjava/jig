package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;

/**
 * メソッド
 */
public class Method {

    MethodDeclaration methodDeclaration;
    DecisionNumber decisionNumber;
    MethodAnnotations methodAnnotations;

    public Method(MethodDeclaration methodDeclaration, DecisionNumber decisionNumber, MethodAnnotations methodAnnotations) {
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
        return methodAnnotations.list().stream()
                .anyMatch(annotatedMethod -> {
                            String annotationName = annotatedMethod.annotationType().fullQualifiedName();
                            // RequestMappingをメタアノテーションとして使うものにしたいが、spring-webに依存させたくないので列挙にする
                            // そのため独自アノテーションに対応できない
                            return annotationName.equals("org.springframework.web.bind.annotation.RequestMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.GetMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.PostMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.PutMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.DeleteMapping")
                                    || annotationName.equals("org.springframework.web.bind.annotation.PatchMapping");
                        }
                )
                && characterizedTypes.stream().pickup(methodDeclaration.declaringType()).has(Characteristic.CONTROLLER);
    }

    public MethodAnnotations methodAnnotations() {
        return methodAnnotations;
    }
}
