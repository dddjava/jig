package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;

/**
 * ビジネスルールの関連
 */
public class BusinessRuleRelation {

    TypeIdentifier from;
    TypeIdentifier to;

    public BusinessRuleRelation(ClassRelation classRelation) {
        from = classRelation.from();
        to = classRelation.to();
    }

    public TypeIdentifier from() {
        return from;
    }

    public TypeIdentifier to() {
        return to;
    }
}
