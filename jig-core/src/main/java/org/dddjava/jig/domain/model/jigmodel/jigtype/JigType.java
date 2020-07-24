package org.dddjava.jig.domain.model.jigmodel.jigtype;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

public class JigType {
    private final TypeDeclaration typeDeclaration;
    private final TypeAlias typeAlias;
    private final TypeKind typeKind;
    private final Visibility visibility;

    public JigType(TypeDeclaration typeDeclaration, TypeAlias typeAlias, TypeKind typeKind, Visibility visibility) {
        this.typeDeclaration = typeDeclaration;
        this.typeAlias = typeAlias;
        this.typeKind = typeKind;
        this.visibility = visibility;
    }

    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public TypeAlias getTypeAlias() {
        return typeAlias;
    }

    public TypeKind getTypeKind() {
        return typeKind;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public TypeIdentifier identifier() {
        return getTypeDeclaration().identifier();
    }
}
