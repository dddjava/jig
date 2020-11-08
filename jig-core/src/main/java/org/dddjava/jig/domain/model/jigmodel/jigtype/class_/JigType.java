package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * JIGが識別する型
 */
public class JigType {
    TypeDeclaration typeDeclaration;

    JigTypeAttribute jigTypeAttribute;

    JigStaticMember jigStaticMember;
    JigInstanceMember jigInstanceMember;

    public JigType(TypeDeclaration typeDeclaration, JigTypeAttribute jigTypeAttribute, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember) {
        this.typeDeclaration = typeDeclaration;
        this.jigTypeAttribute = jigTypeAttribute;
        this.jigStaticMember = jigStaticMember;
        this.jigInstanceMember = jigInstanceMember;
    }

    public TypeDeclaration typeDeclaration() {
        return typeDeclaration;
    }

    public TypeAlias typeAlias() {
        return jigTypeAttribute.alias();
    }

    public TypeKind typeKind() {
        return jigTypeAttribute.kind();
    }

    public Visibility visibility() {
        return jigTypeAttribute.visibility();
    }

    public TypeIdentifier identifier() {
        return typeDeclaration().identifier();
    }

    public JigInstanceMember instanceMember() {
        return jigInstanceMember;
    }

    public JigStaticMember staticMember() {
        return jigStaticMember;
    }
}
