package org.dddjava.jig.domain.model.businessrules.collections;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;

/**
 * コレクション
 */
public class CollectionType {

    TypeIdentifier typeIdentifier;
    MethodDeclarations methodDeclarations;

    public CollectionType(TypeByteCode typeByteCode) {
        this.typeIdentifier = typeByteCode.typeIdentifier();
        this.methodDeclarations = typeByteCode.methodByteCodes().stream().map(MethodByteCode::methodDeclaration).collect(MethodDeclarations.collector());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public MethodDeclarations methods() {
        return methodDeclarations;
    }
}
