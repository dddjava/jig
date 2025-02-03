package org.dddjava.jig.domain.model.data.classes.type;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotations;
import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.JigObjectId;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * JIGが識別する型
 */
public class JigType {
    private final JigTypeHeader jigTypeHeader;
    private final TypeDeclaration typeDeclaration;

    private final JigTypeAttribute jigTypeAttribute;

    private final JigStaticMember jigStaticMember;
    private final JigInstanceMember jigInstanceMember;

    public JigType(JigTypeHeader jigTypeHeader, TypeDeclaration typeDeclaration, JigTypeAttribute jigTypeAttribute, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember) {
        this.jigTypeHeader = jigTypeHeader;
        this.typeDeclaration = typeDeclaration;
        this.jigTypeAttribute = jigTypeAttribute;
        this.jigStaticMember = jigStaticMember;
        this.jigInstanceMember = jigInstanceMember;
    }

    public TypeIdentifier identifier() {
        return TypeIdentifier.valueOf(jigTypeHeader.id().value());
    }

    // HeaderのほうもId<JigType>にする？
    public JigObjectId<JigTypeHeader> id() {
        return jigTypeHeader.id();
    }

    public JigTypeHeader jigTypeHeader() {
        return jigTypeHeader;
    }

    public TypeKind typeKind() {
        return jigTypeAttribute.kind();
    }

    public TypeVisibility visibility() {
        return jigTypeAttribute.visibility();
    }

    public JigInstanceMember instanceMember() {
        return jigInstanceMember;
    }

    public JigStaticMember staticMember() {
        return jigStaticMember;
    }

    public TypeIdentifiers usingTypes() {
        Set<TypeIdentifier> set = new HashSet<>();
        set.addAll(typeDeclaration.listTypeIdentifiers());
        set.addAll(jigTypeAttribute.listUsingTypes());
        set.addAll(jigStaticMember.listUsingTypes());
        set.addAll(jigInstanceMember.listUsingTypes());
        return new TypeIdentifiers(new ArrayList<>(set));
    }

    public PackageIdentifier packageIdentifier() {
        return identifier().packageIdentifier();
    }

    public String simpleName() {
        return jigTypeHeader.simpleName();
    }

    public String fqn() {
        return jigTypeHeader.fqn();
    }

    public String label() {
        return classComment().asTextOrIdentifierSimpleText();
    }

    public JigTypeDescription description() {
        return jigTypeAttribute.description();
    }

    public JigMethods instanceMethods() {
        return instanceMember().instanceMethods()
                .filterProgrammerDefined()
                .excludeNotNoteworthyObjectMethod();
    }

    @Deprecated // ドキュメントでフィールドのアノテーションを参照するためにinstanceJigFieldsに乗り換える
    public FieldDeclarations instanceFields() {
        return instanceJigFields().fieldDeclarations();
    }

    public JigFields instanceJigFields() {
        return instanceMember().instanceFields();
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

    public boolean markedCore() {
        return jigTypeAttribute.classcomment().asText().startsWith("*");
    }

    public boolean isDeprecated() {
        return hasAnnotation(TypeIdentifier.from(Deprecated.class));
    }

    public Annotations annotationsOf(TypeIdentifier typeIdentifier) {
        return jigTypeAttribute.annotationsOf(typeIdentifier);
    }

    public Stream<JigMethod> allJigMethodStream() {
        return Stream.concat(
                instanceMember().jigMethodStream(),
                staticMember().jigMethodStream());
    }

    public TypeCategory typeCategory() {
        return jigTypeAttribute.typeCategory();
    }

    public TypeIdentifier typeIdentifier() {
        return identifier();
    }

    public ClassComment classComment() {
        return jigTypeAttribute.classcomment();
    }

    public String nodeLabel() {
        return classComment().nodeLabel();
    }

    public String nodeLabel(String delimiter) {
        return classComment().nodeLabel(delimiter);
    }
}
