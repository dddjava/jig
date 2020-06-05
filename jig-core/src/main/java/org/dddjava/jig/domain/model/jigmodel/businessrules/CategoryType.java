package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * 区分
 */
public class CategoryType {
    TypeIdentifier typeIdentifier;
    boolean behaviour;
    boolean polymorphism;
    boolean parameterized;

    public CategoryType(TypeKind typeKind, TypeIdentifier typeIdentifier, boolean parameterized, boolean behaviour) {
        this.typeIdentifier = typeIdentifier;
        this.parameterized = parameterized;
        this.behaviour = behaviour;
        this.polymorphism = typeKind == TypeKind.抽象列挙型;
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
