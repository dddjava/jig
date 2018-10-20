package org.dddjava.jig.domain.model.networks.businessrule;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.networks.type.TypeDependency;

/**
 * ビジネスルールの関連
 */
public class BusinessRuleRelation {

    TypeIdentifier from;
    TypeIdentifier to;

    public BusinessRuleRelation(TypeDependency typeDependency) {
        from = typeDependency.from();
        to = typeDependency.to();
    }

    public TypeIdentifier from() {
        return from;
    }

    public TypeIdentifier to() {
        return to;
    }
}
