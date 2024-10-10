package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.parts.classes.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.*;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * メソッドの実装から読み取れること
 */
public class JigMethodBuilder {

    MethodDeclaration methodDeclaration;
    Visibility visibility;
    MethodDerivation methodDerivation;
    List<TypeIdentifier> throwsTypes;

    List<Annotation> annotations;

    List<FieldDeclaration> fieldInstructions;
    List<MethodDeclaration> methodInstructions;

    List<TypeIdentifier> classReferenceCalls;
    List<TypeIdentifier> invokeDynamicTypes;

    Set<TypeIdentifier> useTypes = new HashSet<>();

    // 制御が飛ぶ処理がある（ifやbreak）
    private int jumpInstructionNumber;
    // switchがある
    private int lookupSwitchInstructionNumber;
    // nullを参照している
    private boolean hasReferenceNull;
    // nullによる判定がある
    boolean hasJudgeNull;

    private MethodComment methodComment = null;
    private MethodImplementation methodImplementation = null;

    private JigMethodBuilder(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes, Visibility visibility, MethodDerivation methodDerivation, List<TypeIdentifier> throwsTypes) {
        this.methodDeclaration = methodDeclaration;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;
        this.throwsTypes = throwsTypes;
        this.useTypes.addAll(throwsTypes);

        // TODO useTypesは曖昧なのでなくしたい
        this.useTypes.add(methodDeclaration.methodReturn().typeIdentifier());
        this.useTypes.addAll(methodDeclaration.methodSignature().listArgumentTypeIdentifiers());
        this.useTypes.addAll(useTypes);

        this.annotations = new ArrayList<>();
        this.fieldInstructions = new ArrayList<>();
        this.methodInstructions = new ArrayList<>();
        this.classReferenceCalls = new ArrayList<>();
        this.invokeDynamicTypes = new ArrayList<>();

        this.lookupSwitchInstructionNumber = 0;
        this.jumpInstructionNumber = 0;
        this.hasJudgeNull = false;
        this.hasReferenceNull = false;
    }

    public static JigMethodBuilder constructWithHeader(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes, Visibility visibility, List<TypeIdentifier> throwsTypes, MethodDerivation methodDerivation) {
        return new JigMethodBuilder(
                methodDeclaration,
                useTypes,
                visibility,
                methodDerivation,
                throwsTypes);
    }

    public JigMethod build() {
        return new JigMethod(
                methodDeclaration,
                methodComment != null ? methodComment : MethodComment.empty(methodDeclaration.identifier()),
                hasJudgeNull,
                decisionNumber(),
                annotatedMethods(),
                visibility,
                methodDepend(),
                methodDerivation,
                methodImplementation != null ? methodImplementation : MethodImplementation.unknown(methodDeclaration.identifier()));
    }

    private MethodDepend methodDepend() {
        var useTypes = Stream.of(
                        this.useTypes.stream(),
                        annotations.stream().map(Annotation::typeIdentifier),
                        classReferenceCalls.stream(),
                        invokeDynamicTypes.stream()
                ).flatMap(Function.identity())
                .collect(Collectors.toSet());

        return new MethodDepend(useTypes, fieldInstructions, methodInstructions, hasReferenceNull);
    }

    private MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = annotations.stream()
                .map(annotation -> new MethodAnnotation(annotation, methodDeclaration))
                .collect(Collectors.toList());
        return new MethodAnnotations(methodAnnotations);
    }

    private DecisionNumber decisionNumber() {
        return new DecisionNumber(jumpInstructionNumber + lookupSwitchInstructionNumber);
    }

    public boolean sameSignature(JigMethodBuilder other) {
        return methodDeclaration.methodSignature().isSame(other.methodDeclaration.methodSignature());
    }

    void collectUsingMethodRelations(List<MethodRelation> collector) {
        for (MethodDeclaration usingMethod : methodInstructions) {
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

    public void markReferenceNull() {
        hasReferenceNull = true;
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    public void addFieldInstruction(FieldDeclaration fieldDeclaration) {
        fieldInstructions.add(fieldDeclaration);
    }

    public void addMethodInstruction(MethodDeclaration methodDeclaration) {
        methodInstructions.add(methodDeclaration);
    }

    public void addClassReferenceCall(TypeIdentifier typeIdentifier) {
        classReferenceCalls.add(typeIdentifier);
    }

    public void addInvokeDynamicType(TypeIdentifier typeIdentifier) {
        invokeDynamicTypes.add(typeIdentifier);
    }

    public void addLookupSwitch() {
        lookupSwitchInstructionNumber++;
    }

    public void addJump() {
        jumpInstructionNumber++;
    }

    public void markJudgeNull() {
        hasJudgeNull = true;
    }
}
