package jig.domain.model.specification;

import jig.domain.model.declaration.annotation.MethodAnnotationDeclaration;
import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodSpecification {

    public final MethodDeclaration methodDeclaration;
    private final TypeIdentifier returnType;

    private final Set<TypeIdentifier> useTypes = new HashSet<>();
    private final List<MethodAnnotationDeclaration> methodAnnotationDeclarations = new ArrayList<>();

    private final List<FieldDeclaration> usingFields = new ArrayList<>();
    private final List<MethodDeclaration> usingMethods = new ArrayList<>();

    public MethodSpecification(MethodDeclaration methodDeclaration,
                               TypeIdentifier returnType,
                               List<TypeIdentifier> useTypes) {
        this.methodDeclaration = methodDeclaration;
        this.returnType = returnType;

        this.useTypes.add(returnType);
        this.useTypes.addAll(methodDeclaration.methodSignature().arguments());
        this.useTypes.addAll(useTypes);
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
}
