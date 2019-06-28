package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

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
        return businessRule.typeByteCode().methodDeclarations();
    }

    public CollectionField field() {
        return collectionField;
    }
}
