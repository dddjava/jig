package org.dddjava.jig.infrastructure.asm.data;

/**
 * extendsおよびimplements
 */
public record JigBaseTypeData(JigObjectId<JigTypeData> id, JigBaseTypeAttributeData attributeData) {
    public String simpleName() {
        return id.simpleValue();
    }

    public String simpleNameWithGenerics() {
        return simpleName() + attributeData.typeParametersSimpleName();
    }
}