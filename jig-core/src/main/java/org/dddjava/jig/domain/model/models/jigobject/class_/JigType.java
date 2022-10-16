package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

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

    public ClassComment typeAlias() {
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

    public JigTypeDescription description() {
        return jigTypeAttribute.description();
    }

    public JigMethods instanceMethods() {
        return instanceMember().instanceMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod();
    }

    public FieldDeclarations instanceFields() {
        return instanceMember().instanceFields().fieldDeclarations();
    }

    public JigMethods staticMethods() {
        return staticMember().staticMethods().filterProgrammerDefined();
    }

    public JigTypeValueKind toValueKind() {
        return JigTypeValueKind.from(this);
    }

    public boolean hasAnnotation(TypeIdentifier typeIdentifier) {
        return jigTypeAttribute.hasAnnotation(typeIdentifier);
    }

    public List<Annotation> listAnnotations() {
        return jigTypeAttribute.annotations;
    }

    public boolean implementing(TypeIdentifier typeIdentifier) {
        return typeDeclaration.extendsOrImplements(typeIdentifier);
    }

    public boolean hasSuperOrInterface() {
        return typeDeclaration.hasSuperOrInterface();
    }

    public boolean markedCore() {
        return jigTypeAttribute.alias().asText().startsWith("*");
    }
}
