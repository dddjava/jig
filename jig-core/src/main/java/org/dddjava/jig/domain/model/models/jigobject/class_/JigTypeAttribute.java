package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.parts.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.class_.method.Visibility;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型の属性
 */
public class JigTypeAttribute {
    ClassComment classComment;
    TypeKind typeKind;
    Visibility visibility;

    List<Annotation> annotations;

    public JigTypeAttribute(ClassComment classComment, TypeKind typeKind, Visibility visibility, List<Annotation> annotations) {
        this.classComment = classComment;
        this.typeKind = typeKind;
        this.visibility = visibility;
        this.annotations = annotations;
    }

    public ClassComment alias() {
        return classComment;
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
        return JigTypeDescription.from(classComment.documentationComment());
    }
}
