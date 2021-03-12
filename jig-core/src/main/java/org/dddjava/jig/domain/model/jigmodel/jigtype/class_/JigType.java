package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * JIGが識別する型
 */
public class JigType {
    TypeDeclaration typeDeclaration;

    JigTypeAttribute jigTypeAttribute;

    JigStaticMember jigStaticMember;
    JigInstanceMember jigInstanceMember;

    List<TypeIdentifier> usingTypes;

    public JigType(TypeDeclaration typeDeclaration, JigTypeAttribute jigTypeAttribute, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember, List<TypeIdentifier> usingTypes) {
        this.typeDeclaration = typeDeclaration;
        this.jigTypeAttribute = jigTypeAttribute;
        this.jigStaticMember = jigStaticMember;
        this.jigInstanceMember = jigInstanceMember;
        this.usingTypes = usingTypes;
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

    public TypeIdentifiers usingTypes() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.addAll(typeDeclaration.listTypeIdentifiers());
        list.addAll(jigTypeAttribute.listUsingTypes());
        list.addAll(jigStaticMember.listUsingTypes());
        list.addAll(jigInstanceMember.listUsingTypes());
        list.addAll(usingTypes);
        return new TypeIdentifiers(list);
    }

    public PackageIdentifier packageIdentifier() {
        return identifier().packageIdentifier();
    }

    public String simpleName() {
        return typeDeclaration.identifier().asSimpleText();
    }

    public String fqn() {
        return typeDeclaration.identifier().fullQualifiedName();
    }

    public String label() {
        return typeAlias().asTextOrIdentifierSimpleText();
    }

    public String descriptionText() {
        return jigTypeAttribute.descriptionText();
    }

    public JigMethods instanceMethods() {
        return instanceMember().instanceMethods().excludeCompilerGenerated();
    }

    public JigMethods staticMethods() {
        return staticMember().staticMethods().excludeCompilerGenerated();
    }

    public JigTypeValueKind toValueKind() {
        return JigTypeValueKind.from(this);
    }
}
