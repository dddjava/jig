package org.dddjava.jig.domain.model.models.jigobject.member;

import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JigFields {
    List<JigField> list;

    public JigFields(List<JigField> list) {
        this.list = list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(list.stream().map(jigField -> jigField.fieldDeclaration).collect(Collectors.toList()));
    }

    public TypeIdentifiers typeIdentifies() {
        return list.stream().map(jigField -> jigField.fieldDeclaration.typeIdentifier()).collect(TypeIdentifiers.collector());
    }

    public List<JigField> list() {
        return list;
    }

    public List<TypeIdentifier> listUsingTypes() {
        return list.stream()
                .flatMap(jigField -> {
                    var usingTypes = new ArrayList<TypeIdentifier>();
                    // フィールドの型
                    var fieldType = jigField.fieldDeclaration.fieldType().parameterizedType();
                    usingTypes.add(fieldType.typeIdentifier());
                    // フィールドに型引数がついている場合に追加
                    usingTypes.addAll(fieldType.typeParameters().list());
                    // フィールドにアノテーションがついている場合に追加
                    var annotationTypes = jigField.fieldAnnotations().list().stream().map(fieldAnnotation -> fieldAnnotation.annotationType()).toList();
                    usingTypes.addAll(annotationTypes);

                    return usingTypes.stream()
                            .flatMap(typeIdentifier -> {
                                if (typeIdentifier.isArray()) {
                                    // Type[] の場合は Type[] と Type の2つにする
                                    return Stream.of(typeIdentifier, typeIdentifier.unarray());
                                }
                                return Stream.of(typeIdentifier);
                            });
                })
                .toList();
    }
}
