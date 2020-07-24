package org.dddjava.jig.domain.model.jigmodel.jigtype;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

public class JigType {
    TypeDeclaration typeDeclaration;
    TypeAlias typeAlias;
    TypeKind typeKind;
    Visibility visibility;

    public JigType(TypeDeclaration typeDeclaration, TypeAlias typeAlias, TypeKind typeKind, Visibility visibility) {
        this.typeDeclaration = typeDeclaration;
        this.typeAlias = typeAlias;
        this.typeKind = typeKind;
        this.visibility = visibility;
    }

    public TypeDeclaration typeDeclaration() {
        return typeDeclaration;
    }

    public TypeAlias typeAlias() {
        return typeAlias;
    }

    public TypeKind typeKind() {
        return typeKind;
    }

    public Visibility visibility() {
        return visibility;
    }

    public TypeIdentifier identifier() {
        return typeDeclaration().identifier();
    }
}
