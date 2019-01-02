package org.dddjava.jig.domain.model.collections;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

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
