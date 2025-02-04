package org.dddjava.jig.domain.model.data.types;

/**
 * extendsおよびimplements
 */
public record JigBaseTypeData(JigObjectId<JigTypeHeader> id, JigBaseTypeAttributeData attributeData) {
    public static JigBaseTypeData fromId(JigObjectId<JigTypeHeader> id) {
        return new JigBaseTypeData(id, JigBaseTypeAttributeData.empty());
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