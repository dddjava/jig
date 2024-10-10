package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotations;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * JIGが識別する型
 */
public class JigType {
    private final TypeDeclaration typeDeclaration;

    private final JigTypeAttribute jigTypeAttribute;

    private final JigStaticMember jigStaticMember;
    private final JigInstanceMember jigInstanceMember;

    public JigType(TypeDeclaration typeDeclaration, JigTypeAttribute jigTypeAttribute, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember) {
        this.typeDeclaration = typeDeclaration;
        this.jigTypeAttribute = jigTypeAttribute;
        this.jigStaticMember = jigStaticMember;
        this.jigInstanceMember = jigInstanceMember;
    }

    public TypeIdentifier identifier() {
        return typeDeclaration().identifier();
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
        return jigTypeAttribute.alias().asText().startsWith("*");
    }

    public boolean isDeprecated() {
        return hasAnnotation(TypeIdentifier.from(Deprecated.class));
    }

    public Annotations annotationsOf(TypeIdentifier typeIdentifier) {
        return jigTypeAttribute.annotationsOf(typeIdentifier);
    }

    public Optional<String> interfacePointDescription() {
        var annotations = jigTypeAttribute.annotationsOf(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"));
        return annotations.list().stream()
                // 複数はつけられないので一つで良い
                .findFirst()
                .map(annotation -> annotation.descriptionTextAnyOf("value", "path")
                        // 空文字列や何も設定されていない場合は "/" として扱う
                        .filter(value -> !value.isEmpty()).orElse("/"));
    }

    public String interfaceLabelText() {
        var annotations = jigTypeAttribute.annotationsOf(TypeIdentifier.valueOf("io.swagger.v3.oas.annotations.tags.Tag"));
        return annotations.list().stream().findFirst()
                .flatMap(annotation -> annotation.descriptionTextAnyOf("description"))
                .orElse(label());
    }

    public JigMethods methods() {
        return Stream.concat(instanceMethods().stream(), staticMethods().stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigMethods::new));
    }

    public Stream<JigMethod> methodStream() {
        return Stream.concat(instanceMethods().stream(), staticMethods().stream());
    }
}
