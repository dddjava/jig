package org.dddjava.jig.domain.model.declaration.method;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

/**
 * メソッド戻り値
 */
public class MethodReturn {

    TypeIdentifier typeIdentifier;

    public MethodReturn(TypeIdentifier typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }
}
