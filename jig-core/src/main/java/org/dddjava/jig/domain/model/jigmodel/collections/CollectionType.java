package org.dddjava.jig.domain.model.jigmodel.collections;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * コレクション
 */
public class CollectionType {

    JigType jigType;

    public CollectionType(JigType jigType) {
        this.jigType = jigType;
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public MethodDeclarations methods() {
        return jigType.instanceMember().instanceMethods().declarations();
    }

    public JigType jigType() {
        return jigType;
    }
}
