package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.type.Type;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCode;

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
