package jig.domain.model.specification;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.method.MethodIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodSpecification {

    public final MethodIdentifier identifier;
    private Set<TypeIdentifier> useTypes = new HashSet<>();
    private final TypeIdentifier returnType;
    private final boolean isInstanceMethod;

    public MethodSpecification(MethodIdentifier identifier,
                               TypeIdentifier returnType,
                               List<TypeIdentifier> useTypes,
                               boolean isInstanceMethod) {
        this.returnType = returnType;
        this.identifier = identifier;

        this.useTypes.add(returnType);
        this.useTypes.addAll(identifier.methodSignature().arguments());
        this.useTypes.addAll(useTypes);

        this.isInstanceMethod = isInstanceMethod;
    }

    public final List<FieldIdentifier> usingFields = new ArrayList<>();
    public final List<MethodIdentifier> usingMethods = new ArrayList<>();

    public TypeIdentifier getReturnTypeName() {
        return returnType;
    }

    public void registerUsingField(TypeIdentifier ownerType, FieldIdentifier field) {
        usingFields.add(field);

        useTypes.add(ownerType);
        useTypes.add(field.typeIdentifier());
    }

    public void registerMethodInstruction(TypeIdentifier ownerType, MethodIdentifier methodIdentifier, TypeIdentifier returnType) {
        usingMethods.add(methodIdentifier);

        // メソッドやコンストラクタの持ち主
        // new演算子で呼び出されるコンストラクタの持ち主をここで捕まえる
        useTypes.add(ownerType);

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
}
