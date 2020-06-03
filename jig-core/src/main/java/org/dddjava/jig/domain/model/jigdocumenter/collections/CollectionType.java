package org.dddjava.jig.domain.model.jigdocumenter.collections;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * コレクション
 */
public class CollectionType {

    BusinessRule businessRule;
    CollectionField collectionField;

    public CollectionType(BusinessRule businessRule, CollectionField collectionField) {
        this.businessRule = businessRule;
        this.collectionField = collectionField;
    }

    public TypeIdentifier typeIdentifier() {
        return businessRule.type().identifier();
    }

    public MethodDeclarations methods() {
        return businessRule.methodDeclarations();
    }

    public CollectionField field() {
        return collectionField;
    }
}
