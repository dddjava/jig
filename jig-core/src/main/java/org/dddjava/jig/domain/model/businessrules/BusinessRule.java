package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.Type;

/**
 * ビジネスルール
 */
public class BusinessRule {

    TypeByteCode typeByteCode;

    public BusinessRule(TypeByteCode typeByteCode) {
        this.typeByteCode = typeByteCode;
    }

    public Type type() {
        return typeByteCode.type();
    }

    public TypeByteCode typeByteCode() {
        return typeByteCode;
    }
}
