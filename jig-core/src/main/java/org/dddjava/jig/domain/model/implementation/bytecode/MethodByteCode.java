package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethod;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
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
    private final List<AnnotatedMethod> annotatedMethods = new ArrayList<>();

    private final List<FieldDeclaration> usingFields = new ArrayList<>();
    private final List<MethodDeclaration> usingMethods = new ArrayList<>();

    // 制御が飛ぶ処理がある（ifやbreak）
    private int jumpInstructionNumber = 0;
    // switchがある
    private int lookupSwitchInstructionNumber = 0;

    public MethodByteCode(MethodDeclaration methodDeclaration,
                          List<TypeIdentifier> useTypes,
                          int access) {
        this.methodDeclaration = methodDeclaration;
        this.access = access;

        this.useTypes.add(methodDeclaration.returnType());
        this.useTypes.addAll(methodDeclaration.methodSignature().arguments());
        this.useTypes.addAll(useTypes);
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
        useTypes.add(methodDeclaration.returnType());
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

    public void registerAnnotation(AnnotatedMethod annotatedMethod) {
        annotatedMethods.add(annotatedMethod);
        useTypes.add(annotatedMethod.annotationType());
    }

    public void registerInvokeDynamic(TypeIdentifier type) {
        useTypes.add(type);
    }

    public List<AnnotatedMethod> annotatedMethods() {
        return annotatedMethods;
    }

    public Set<TypeIdentifier> useTypes() {
        return useTypes;
    }

    public FieldDeclarations usingFields() {
        return usingFields.stream().collect(FieldDeclarations.collector());
    }

    public MethodDeclarations usingMethods() {
        return usingMethods.stream().collect(MethodDeclarations.collector());
    }

    boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public void bind(ByteCode byteCode) {
        MethodKind.methodKind(this).bind(this, byteCode);
    }

    public Accessor accessor() {
        if ((access & Opcodes.ACC_PUBLIC) != 0) return Accessor.PUBLIC;
        return Accessor.NOT_PUBLIC;
    }

    public Method method() {
        return new Method(methodDeclaration, new DecisionNumber(jumpInstructionNumber + lookupSwitchInstructionNumber), annotatedMethods);
    }
}
