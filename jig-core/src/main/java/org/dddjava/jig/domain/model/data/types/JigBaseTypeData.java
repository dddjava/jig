package org.dddjava.jig.domain.model.data.types;

/**
 * extendsおよびimplements
 */
public record JigBaseTypeData(TypeIdentifier id, JigBaseTypeAttributeData attributeData) {
    public static JigBaseTypeData fromId(TypeIdentifier id) {
        return new JigBaseTypeData(id, JigBaseTypeAttributeData.empty());
    }

    public static JigBaseTypeData fromJvmBinaryName(String jvmBinaryName) {
        return fromId(TypeIdentifier.fromJvmBinaryName(jvmBinaryName));
    }

    public String simpleName() {
        return id.simpleValue();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + attributeData.typeArgumentSimpleName();
    }

    public String fqnWithGenerics() {
        return id.value() + attributeData.typeArgumentsFqn();
    }

    public String fqn() {
        return id.value();
    }
}