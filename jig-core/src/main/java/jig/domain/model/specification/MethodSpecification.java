package jig.domain.model.specification;

import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodSpecification {

    public final MethodDeclaration methodDeclaration;
    private Set<TypeIdentifier> useTypes = new HashSet<>();
    private final TypeIdentifier returnType;
    private final boolean isInstanceMethod;

    public MethodSpecification(MethodDeclaration methodDeclaration,
                               TypeIdentifier returnType,
                               List<TypeIdentifier> useTypes,
                               boolean isInstanceMethod) {
        this.returnType = returnType;
        this.methodDeclaration = methodDeclaration;

        this.useTypes.add(returnType);
        this.useTypes.addAll(methodDeclaration.methodSignature().arguments());
        this.useTypes.addAll(useTypes);

        this.isInstanceMethod = isInstanceMethod;
    }

    public final List<FieldDeclaration> usingFields = new ArrayList<>();
    public final List<MethodDeclaration> usingMethods = new ArrayList<>();

    public TypeIdentifier getReturnTypeName() {
        return returnType;
    }

    public void registerFieldInstruction(FieldDeclaration field) {
        usingFields.add(field);

        useTypes.add(field.declaringType());
        useTypes.add(field.typeIdentifier());
    }

    public void registerMethodInstruction(MethodDeclaration methodDeclaration, TypeIdentifier returnType) {
        usingMethods.add(methodDeclaration);

        // メソッドやコンストラクタの持ち主
        // new演算子で呼び出されるコンストラクタの持ち主をここで捕まえる
        useTypes.add(methodDeclaration.declaringType());

        // 呼び出したメソッドの戻り値の型
        useTypes.add(returnType);
    }

    public void registerClassReference(TypeIdentifier type) {
        useTypes.add(type);
    }

    public Set<TypeIdentifier> useTypes() {
        return useTypes;
    }

    public boolean isInstanceMethod() {
        return isInstanceMethod;
    }

    public void registerAnnotation(TypeIdentifier type) {
        useTypes.add(type);
    }

    public void registerInvokeDynamic(TypeIdentifier type) {
        useTypes.add(type);
    }
}
