package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.implementation.declaration.type.Type;

/**
 * ビジネスルール
 */
public class BusinessRule {

    private final Type type;

    public BusinessRule(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }
}
