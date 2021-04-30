package org.dddjava.jig.domain.model.jigsource.analyzed;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.parts.alias.MethodAlias;
import org.dddjava.jig.domain.model.parts.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.parts.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.parts.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.method.*;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.relation.method.MethodDepend;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * メソッドの実装から読み取れること
 */
public class MethodFact {

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
    private final boolean hasReferenceNull;
    // nullによる判定がある
    boolean hasJudgeNull;

    private MethodAlias methodAlias;

    public MethodFact(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes, Visibility visibility, MethodDerivation methodDerivation, List<Annotation> annotations, List<TypeIdentifier> throwsTypes, List<FieldDeclaration> fieldInstructions, List<MethodDeclaration> methodInstructions, List<TypeIdentifier> classReferenceCalls, List<TypeIdentifier> invokeDynamicTypes, int lookupSwitchInstructionNumber, int jumpInstructionNumber, boolean hasJudgeNull, boolean hasReferenceNull) {
        this.methodDeclaration = methodDeclaration;
        this.visibility = visibility;
        this.methodDerivation = methodDerivation;
        this.throwsTypes = throwsTypes;
        this.useTypes.addAll(throwsTypes);

        // TODO useTypesは曖昧なのでなくしたい
        this.useTypes.add(methodDeclaration.methodReturn().typeIdentifier());
        this.useTypes.addAll(methodDeclaration.methodSignature().arguments());
        this.useTypes.addAll(useTypes);

        this.annotations = annotations;
        annotations.forEach(annotation -> this.useTypes.add(annotation.typeIdentifier()));

        this.fieldInstructions = fieldInstructions;
        this.methodInstructions = methodInstructions;

        this.classReferenceCalls = classReferenceCalls;
        this.useTypes.addAll(classReferenceCalls);

        this.invokeDynamicTypes = invokeDynamicTypes;
        this.useTypes.addAll(invokeDynamicTypes);

        this.lookupSwitchInstructionNumber = lookupSwitchInstructionNumber;
        this.jumpInstructionNumber = jumpInstructionNumber;
        this.hasJudgeNull = hasJudgeNull;
        this.hasReferenceNull = hasReferenceNull;

        this.methodAlias = MethodAlias.empty(methodDeclaration.identifier());
    }

    public JigMethod createMethod() {
        return new JigMethod(
                methodDeclaration,
                methodAlias,
                judgeNull(),
                decisionNumber(),
                annotatedMethods(),
                visibility,
                methodDepend(),
                methodDerivation);
    }

    public MethodDepend methodDepend() {
        return new MethodDepend(useTypes, fieldInstructions, methodInstructions, hasReferenceNull);
    }

    public MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = annotations.stream()
                .map(annotation -> new MethodAnnotation(annotation, methodDeclaration))
                .collect(Collectors.toList());
        return new MethodAnnotations(methodAnnotations);
    }

    public DecisionNumber decisionNumber() {
        return new DecisionNumber(jumpInstructionNumber + lookupSwitchInstructionNumber);
    }

    public boolean sameSignature(MethodFact other) {
        return methodDeclaration.methodSignature().isSame(other.methodDeclaration.methodSignature());
    }

    public boolean judgeNull() {
        return hasJudgeNull;
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

    public void registerMethodAlias(MethodAlias methodAlias) {
        this.methodAlias = methodAlias;
    }
}
