package org.dddjava.jig.domain.model.data.types;

/**
 * extendsおよびimplements
 */
public record JigBaseTypeData(JigObjectId<JigTypeHeader> id, JigBaseTypeAttributeData attributeData) {
    public static JigBaseTypeData fromNameOnly(String fqn) {
        return new JigBaseTypeData(new JigObjectId<>(fqn), JigBaseTypeAttributeData.empty());
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
}