package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.data.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.method.instruction.MethodInstructions;
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

    List<FieldDeclaration> fieldInstructions;


    private final MethodInstructions methodInstructions;

    private MethodComment methodComment = null;
    private MethodImplementation methodImplementation = null;

    private JigMethodBuilder(MethodDeclaration methodDeclaration, List<TypeIdentifier> signatureContainedTypes, Visibility visibility, MethodDerivation methodDerivation, List<TypeIdentifier> throwsTypes, MethodInstructions methodInstructions) {
        this.methodDeclaration = methodDeclaration;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;

        this.throwsTypes = throwsTypes;
        this.signatureContainedTypes = signatureContainedTypes;

        this.annotations = new ArrayList<>();
        this.fieldInstructions = new ArrayList<>();

        this.methodInstructions = methodInstructions;
    }

    public static JigMethodBuilder constructWithHeader(MethodDeclaration methodDeclaration, List<TypeIdentifier> signatureContainedTypes, Visibility visibility, List<TypeIdentifier> throwsTypes, MethodDerivation methodDerivation, MethodInstructions methodInstructions) {
        return new JigMethodBuilder(
                methodDeclaration,
                signatureContainedTypes,
                visibility,
                methodDerivation,
                throwsTypes,
                methodInstructions);
    }

    public JigMethod build() {
        return new JigMethod(
                methodDeclaration,
                methodComment != null ? methodComment : MethodComment.empty(methodDeclaration.identifier()),
                annotatedMethods(),
                visibility,
                new MethodDepend(methodInstructions),
                methodDerivation,
                methodImplementation != null ? methodImplementation : MethodImplementation.unknown(methodDeclaration.identifier()),
                methodInstructions,
                throwsTypes,
                signatureContainedTypes);
    }

    private MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = annotations.stream()
                .map(annotation -> new MethodAnnotation(annotation, methodDeclaration))
                .collect(Collectors.toList());
        return new MethodAnnotations(methodAnnotations);
    }

    void collectUsingMethodRelations(List<MethodRelation> collector) {
        for (MethodDeclaration usingMethod : methodInstructions.instructMethods().list()) {
            MethodRelation methodRelation = new MethodRelation(methodDeclaration, usingMethod);
            collector.add(methodRelation);
        }
    }

    public MethodIdentifier methodIdentifier() {
        return methodDeclaration.identifier();
    }

    public void registerMethodAlias(MethodComment methodComment) {
        this.methodComment = methodComment;
    }

    public void applyTextSource(TextSourceModel textSourceModel) {
        textSourceModel.methodImplementation(methodDeclaration.identifier())
                .ifPresent(methodImplementation -> this.methodImplementation = methodImplementation);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }
}
