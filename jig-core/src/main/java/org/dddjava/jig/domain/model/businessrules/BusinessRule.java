package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.Type;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

/**
 * ビジネスルール
 */
public class BusinessRule {

    TypeByteCode typeByteCode;
    BusinessRuleFields businessRuleFields;

    public BusinessRule(TypeByteCode typeByteCode) {
        this.typeByteCode = typeByteCode;
        this.businessRuleFields = new BusinessRuleFields(typeByteCode.fieldDeclarations());
    }

    public Type type() {
        return typeByteCode.type();
    }

    @Deprecated
    public TypeByteCode typeByteCode() {
        return typeByteCode;
    }

    boolean isCollection() {
        return businessRuleFields.satisfyCollection();
    }

    public BusinessRuleFields fields() {
        return businessRuleFields;
    }

    boolean isCategory() {
        return typeByteCode.isEnum();
    }

    public TypeIdentifier typeIdentifier() {
        return typeByteCode.typeIdentifier();
    }

    public MethodDeclarations methodDeclarations() {
        return typeByteCode.methodDeclarations();
    }
}
