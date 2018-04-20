package jig.domain.model.specification;

import jig.domain.model.definition.field.FieldDefinition;
import jig.domain.model.definition.method.MethodDefinition;
import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodSpecification {

    public final MethodDefinition methodDefinition;
    private Set<TypeIdentifier> useTypes = new HashSet<>();
    private final TypeIdentifier returnType;
    private final boolean isInstanceMethod;

    public MethodSpecification(MethodDefinition methodDefinition,
                               TypeIdentifier returnType,
                               List<TypeIdentifier> useTypes,
                               boolean isInstanceMethod) {
        this.returnType = returnType;
        this.methodDefinition = methodDefinition;

        this.useTypes.add(returnType);
        this.useTypes.addAll(methodDefinition.methodSignature().arguments());
        this.useTypes.addAll(useTypes);

        this.isInstanceMethod = isInstanceMethod;
    }

    public final List<FieldDefinition> usingFields = new ArrayList<>();
    public final List<MethodDefinition> usingMethods = new ArrayList<>();

    public TypeIdentifier getReturnTypeName() {
        return returnType;
    }

    public void registerFieldInstruction(FieldDefinition field) {
        usingFields.add(field);

        useTypes.add(field.declaringType());
        useTypes.add(field.typeIdentifier());
    }

    public void registerMethodInstruction(MethodDefinition methodDefinition, TypeIdentifier returnType) {
        usingMethods.add(methodDefinition);

        // メソッドやコンストラクタの持ち主
        // new演算子で呼び出されるコンストラクタの持ち主をここで捕まえる
        useTypes.add(methodDefinition.declaringType());

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
