package org.dddjava.jig.infrastructure.asm.data;

/**
 * extendsおよびimplements
 */
public record JigBaseTypeData(JigObjectId<JigTypeHeader> id, JigBaseTypeAttributeData attributeData) {
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