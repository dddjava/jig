package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.parts.alias.TypeAlias;
import org.dddjava.jig.domain.model.parts.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.method.Visibility;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型の属性
 */
public class JigTypeAttribute {
    TypeAlias typeAlias;
    TypeKind typeKind;
    Visibility visibility;

    List<Annotation> annotations;

    public JigTypeAttribute(TypeAlias typeAlias, TypeKind typeKind, Visibility visibility, List<Annotation> annotations) {
        this.typeAlias = typeAlias;
        this.typeKind = typeKind;
        this.visibility = visibility;
        this.annotations = annotations;
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

    List<TypeIdentifier> listUsingTypes() {
        // TODO アノテーションの属性に書かれる型が拾えていない
        return annotations.stream()
                .map(annotation -> annotation.typeIdentifier())
                .collect(Collectors.toList());
    }

    public JigTypeDescription description() {
        return JigTypeDescription.from(typeAlias.documentationComment());
    }
}
