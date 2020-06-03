package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {
    TypeIdentifier typeIdentifier;
    boolean behaviour;
    boolean polymorphism;
    boolean parameterized;

    public CategoryType(TypeIdentifier typeIdentifier, boolean parameterized, boolean behaviour, boolean polymorphism) {
        this.typeIdentifier = typeIdentifier;
        this.parameterized = parameterized;
        this.behaviour = behaviour;
        this.polymorphism = polymorphism;
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
