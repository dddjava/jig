package org.dddjava.jig.domain.model.implementation;

import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodSpecification {

    public final MethodDeclaration methodDeclaration;
    private final TypeIdentifier returnType;
    private final int access;

    private final Set<TypeIdentifier> useTypes = new HashSet<>();
    private final List<MethodAnnotationDeclaration> methodAnnotationDeclarations = new ArrayList<>();

    private final List<FieldDeclaration> usingFields = new ArrayList<>();
    private final List<MethodDeclaration> usingMethods = new ArrayList<>();

    // 制御が飛ぶ処理がある（ifやbreak）
    private boolean jumpInstruction = false;
    // switchがある
    private boolean lookupSwitchInstruction = false;

    public MethodSpecification(MethodDeclaration methodDeclaration,
                               TypeIdentifier returnType,
                               List<TypeIdentifier> useTypes,
                               int access) {
        this.methodDeclaration = methodDeclaration;
        this.returnType = returnType;
        this.access = access;

        this.useTypes.add(returnType);
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
        this.jumpInstruction = true;
    }

    public void registerLookupSwitchInstruction() {
        this.lookupSwitchInstruction = true;
    }

    public void registerClassReference(TypeIdentifier type) {
        useTypes.add(type);
    }

    public void registerAnnotation(MethodAnnotationDeclaration methodAnnotationDeclaration) {
        methodAnnotationDeclarations.add(methodAnnotationDeclaration);
        useTypes.add(methodAnnotationDeclaration.annotationType());
    }

    public void registerInvokeDynamic(TypeIdentifier type) {
        useTypes.add(type);
    }

    public TypeIdentifier returnType() {
        return returnType;
    }

    public List<MethodAnnotationDeclaration> methodAnnotationDeclarations() {
        return methodAnnotationDeclarations;
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

    public boolean hasDecision() {
        return jumpInstruction || lookupSwitchInstruction;
    }

    boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    public void bind(Specification specification) {
        MethodType.methodType(this).bind(this, specification);
    }
}
