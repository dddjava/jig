package org.dddjava.jig.domain.model.data.classes.type;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.Annotations;
import org.dddjava.jig.domain.model.data.types.TypeVisibility;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 型の属性
 */
public class JigTypeAttribute {
    private final ClassComment classComment;
    private final TypeKind typeKind;
    private final TypeVisibility visibility;

    private final List<Annotation> annotations;

    public JigTypeAttribute(ClassComment classComment, TypeKind typeKind, TypeVisibility visibility, List<Annotation> annotations) {
        this.classComment = classComment;
        this.typeKind = typeKind;
        this.visibility = visibility;
        this.annotations = annotations;
    }

    public ClassComment classcomment() {
        return classComment;
    }

    public TypeKind kind() {
        return typeKind;
    }

    public TypeVisibility visibility() {
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

    public boolean hasAnnotation(TypeIdentifier typeIdentifier) {
        return annotations.stream()
                .anyMatch(annotation -> annotation.is(typeIdentifier));
    }

    public Annotations annotationsOf(TypeIdentifier typeIdentifier) {
        return new Annotations(annotations).filterAny(typeIdentifier);
    }

    public TypeCategory typeCategory() {
        // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Service"))
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.annotation.Service.class))) {
            return TypeCategory.Usecase;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Controller"))
                || hasAnnotation(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RestController"))
                || hasAnnotation(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.ControllerAdvice"))
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.adapter.HandleDocument.class))) {
            return TypeCategory.InputAdapter;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Repository"))
                || hasAnnotation(TypeIdentifier.from(org.dddjava.jig.annotation.Repository.class))) {
            return TypeCategory.OutputAdapter;
        }
        if (hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Component"))) {
            return TypeCategory.BoundaryComponent;
        }

        return TypeCategory.Others;
    }
}
