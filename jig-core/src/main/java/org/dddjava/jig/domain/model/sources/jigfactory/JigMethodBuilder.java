package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.method.instruction.Instructions;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの実装から読み取れること
 */
public class JigMethodBuilder {

    MethodDeclaration methodDeclaration;
    Visibility visibility;
    MethodDerivation methodDerivation;
    List<TypeIdentifier> throwsTypes;
    private final List<TypeIdentifier> signatureContainedTypes;

    List<Annotation> annotations;


    private final Instructions instructions;

    private MethodImplementation methodImplementation = null;

    public JigMethodBuilder(MethodDeclaration methodDeclaration, List<TypeIdentifier> signatureContainedTypes, Visibility visibility, MethodDerivation methodDerivation, List<TypeIdentifier> throwsTypes, Instructions instructions) {
        this.methodDeclaration = methodDeclaration;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;
        this.throwsTypes = throwsTypes;
        this.signatureContainedTypes = signatureContainedTypes;

        this.annotations = new ArrayList<>();

        this.instructions = instructions;
    }

    public JigMethod build() {
        return new JigMethod(
                methodDeclaration,
                annotatedMethods(), visibility, methodDerivation, instructions, throwsTypes, signatureContainedTypes,
                methodImplementation != null ? methodImplementation : MethodImplementation.unknown(methodDeclaration.identifier())
        );
    }

    private MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = annotations.stream()
                .map(annotation -> new MethodAnnotation(annotation, methodDeclaration))
                .collect(Collectors.toList());
        return new MethodAnnotations(methodAnnotations);
    }

    void collectUsingMethodRelations(List<MethodRelation> collector) {
        for (MethodDeclaration usingMethod : instructions.instructMethods().list()) {
            MethodRelation methodRelation = new MethodRelation(methodDeclaration, usingMethod);
            collector.add(methodRelation);
        }
    }

    public MethodIdentifier methodIdentifier() {
        return methodDeclaration.identifier();
    }

    public void registerMethodImplementation(MethodImplementation methodImplementation) {
        this.methodImplementation = methodImplementation;
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }
}
