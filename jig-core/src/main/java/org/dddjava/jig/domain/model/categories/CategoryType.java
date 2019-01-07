package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

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
}
