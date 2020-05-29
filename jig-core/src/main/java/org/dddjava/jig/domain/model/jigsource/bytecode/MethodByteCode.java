package org.dddjava.jig.domain.model.jigsource.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.method.Accessor;
import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodDepend;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * メソッドの実装
 */
public class MethodByteCode {

    public final MethodDeclaration methodDeclaration;
    private final int access;

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

    public MethodByteCode(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes, int access) {
        this.methodDeclaration = methodDeclaration;
        this.access = access;

        this.useTypes.add(methodDeclaration.methodReturn().typeIdentifier());
        this.useTypes.addAll(methodDeclaration.methodSignature().arguments());
        this.useTypes.addAll(useTypes);
    }

    public MethodDepend methodDepend() {
        return new MethodDepend(useTypes(), usingFields, usingMethods, hasNullReference);
    }

    public void registerFieldInstruction(FieldDeclaration field) {
        usingFields.add(field);

        useTypes.add(field.declaringType());
        useTypes.add(field.typeIdentifier());
    }

    public void registerMethodInstruction(MethodDeclaration methodDeclaration) {
        usingMethods.add(methodDeclaration);

        // メソッドやコンストラクタの持ち主
        // new演算子で呼び出されるコンストラクタの持ち主をここで捕まえる
        useTypes.add(methodDeclaration.declaringType());

        // 呼び出したメソッドの戻り値の型
        useTypes.add(methodDeclaration.methodReturn().typeIdentifier());
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

    public Set<TypeIdentifier> useTypes() {
        return useTypes;
    }

    boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public void bind(TypeByteCode typeByteCode) {
        MethodKind.methodKind(this).bind(this, typeByteCode);
    }

    public Accessor accessor() {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return Accessor.PUBLIC;
        return Accessor.NOT_PUBLIC;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public DecisionNumber decisionNumber() {
        return new DecisionNumber(jumpInstructionNumber + lookupSwitchInstructionNumber);
    }

    public boolean sameSignature(MethodByteCode other) {
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
