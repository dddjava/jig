package org.dddjava.jig.domain.model.boolquerymethod;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

/**
 * 真偽値を返すモデルの切り口
 */
public class BoolQueryModelMethodAngle {
    MethodDeclaration method;
    MethodDeclarations usages;

    public BoolQueryModelMethodAngle(MethodDeclaration method, MethodDeclarations usages) {
        this.method = method;
        this.usages = usages;
    }

    public TypeIdentifier declaringTypeIdentifier() {
        return method.declaringType();
    }

    public MethodDeclaration method() {
        return method;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        return usages.declaringTypes();
    }
}
