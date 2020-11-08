package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;

/**
 * 型の属性
 */
public class JigTypeAttribute {
    TypeAlias typeAlias;
    TypeKind typeKind;
    Visibility visibility;

    public JigTypeAttribute(TypeAlias typeAlias, TypeKind typeKind, Visibility visibility) {
        this.typeAlias = typeAlias;
        this.typeKind = typeKind;
        this.visibility = visibility;
    }

    public TypeAlias alias() {
        return typeAlias;
    }

    public TypeKind kind() {
        return typeKind;
    }

    public Visibility visibility() {
        return visibility;
    }
}
