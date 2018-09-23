package org.dddjava.jig.domain.model.progresses;

import org.dddjava.jig.domain.model.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * 進捗
 */
public class ProgressAngle {
    MethodDeclaration methodDeclaration;
    TypeAnnotations typeAnnotations;
    MethodAnnotations methodAnnotations;
    //TODO: アノテーション型に依存したくない
    TypeIdentifier annotationType = new TypeIdentifier(org.dddjava.jig.annotation.Progress.class);

    public ProgressAngle(MethodDeclaration methodDeclaration, TypeAnnotations typeAnnotations, MethodAnnotations methodAnnotations) {
        this.methodDeclaration = methodDeclaration;
        this.typeAnnotations = typeAnnotations;
        this.methodAnnotations = methodAnnotations;
    }

    public String asText() {
        String method = methodProgressText();
        return method.isEmpty() ? typeProgressText() : method;
    }

    public boolean matches(MethodDeclaration methodDeclaration) {
        return this.methodDeclaration.sameIdentifier(methodDeclaration);
    }

    private String methodProgressText() {
        MethodAnnotations methodAnnotations = this.methodAnnotations.filter(this.methodDeclaration);
        return getAnnotationValue(methodAnnotations.annotations());

    }

    private String typeProgressText() {
        TypeIdentifier declaringType = this.methodDeclaration.declaringType();
        TypeAnnotations typeAnnotations = this.typeAnnotations.filter(declaringType);
        return getAnnotationValue(typeAnnotations.annotations());
    }

    private String getAnnotationValue(Annotations annotations2) {
        Annotations annotations = annotations2.filterAny(annotationType);
        return annotations.descriptionTextsOf("value").stream().findAny().orElse("");
    }
}
