package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodDepend;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * メソッドの実装から読み取れること
 */
public class MethodFact {

    public final MethodDeclaration methodDeclaration;
    private final MethodKind methodKind;
    private final Visibility visibility;

    private final Set<TypeIdentifier> useTypes = new HashSet<>();
    private final List<MethodAnnotation> methodAnnotations = new ArrayList<>();

    private final List<FieldDeclaration> usingFields = new ArrayList<>();
    private final List<MethodDeclaration> usingMethods = new ArrayList<>();

    // 制御が飛ぶ処理がある（ifやbreak）
    private int jumpInstructionNumber = 0;
    // switchがある
    private int lookupSwitchInstructionNumber = 0;

    /** nullを参照している */
    private boolean hasNullReference = false;

    public MethodFact(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes,
                      MethodKind methodKind, Visibility visibility) {
        this.methodDeclaration = methodDeclaration;
        this.methodKind = methodKind;
        this.visibility = visibility;

        this.useTypes.add(methodDeclaration.methodReturn().typeIdentifier());
        this.useTypes.addAll(methodDeclaration.methodSignature().arguments());
        this.useTypes.addAll(useTypes);
    }

    public MethodDepend methodDepend() {
        return new MethodDepend(useTypes, usingFields, usingMethods, hasNullReference);
    }

    public void registerFieldInstruction(FieldDeclaration field) {
        usingFields.add(field);
    }

    public void registerMethodInstruction(MethodDeclaration methodDeclaration) {
        usingMethods.add(methodDeclaration);
    }

    public void registerJumpInstruction() {
        this.jumpInstructionNumber++;
    }

    public void registerLookupSwitchInstruction() {
        this.lookupSwitchInstructionNumber++;
    }

    public void registerClassReference(TypeIdentifier type) {
        useTypes.add(type);
    }

    public void registerAnnotation(MethodAnnotation methodAnnotation) {
        methodAnnotations.add(methodAnnotation);
        useTypes.add(methodAnnotation.annotationType());
    }

    public void registerInvokeDynamic(TypeIdentifier type) {
        useTypes.add(type);
    }

    public MethodAnnotations annotatedMethods() {
        return new MethodAnnotations(methodAnnotations);
    }

    public void bind(TypeFact typeFact) {
        methodKind.bind(this, typeFact);
    }

    public Visibility visibility() {
        return visibility;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return new DecisionNumber(jumpInstructionNumber + lookupSwitchInstructionNumber);
    }

    public boolean sameSignature(MethodFact other) {
        return methodDeclaration().methodSignature().isSame(other.methodDeclaration().methodSignature());
    }

    public void markReferenceNull() {
        hasNullReference = true;
    }

    boolean hasJudgeNull;

    public void markJudgeNull() {
        hasJudgeNull = true;
    }

    public boolean judgeNull() {
        return hasJudgeNull;
    }
}
