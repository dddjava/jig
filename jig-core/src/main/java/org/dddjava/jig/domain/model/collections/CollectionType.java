package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

/**
 * コレクション
 */
public class CollectionType {

    BusinessRule businessRule;

    public CollectionType(BusinessRule businessRule) {
        this.businessRule = businessRule;
    }

    public TypeIdentifier typeIdentifier() {
        return businessRule.type().identifier();
    }

    public MethodDeclarations methods() {
        return businessRule.typeByteCode().methodDeclarations();
    }

    public TypeIdentifier includeType() {
        // TODO コレクションで扱っている型 https://github.com/dddjava/Jig/issues/54
        throw new UnsupportedOperationException();
    }
}
