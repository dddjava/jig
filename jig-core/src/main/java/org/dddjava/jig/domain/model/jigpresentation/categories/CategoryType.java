package org.dddjava.jig.domain.model.jigpresentation.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;

/**
 * 区分
 */
public class CategoryType {
    TypeIdentifier typeIdentifier;
    boolean behaviour;
    boolean polymorphism;
    boolean parameterized;

    public CategoryType(TypeByteCode typeByteCode) {
        this.typeIdentifier = typeByteCode.typeIdentifier();
        this.parameterized = typeByteCode.hasField();
        this.behaviour = typeByteCode.hasInstanceMethod();
        this.polymorphism = typeByteCode.canExtend();
    }

    public boolean hasParameter() {
        return parameterized;
    }

    public boolean hasBehaviour() {
        return behaviour;
    }

    public boolean isPolymorphism() {
        return polymorphism;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}
