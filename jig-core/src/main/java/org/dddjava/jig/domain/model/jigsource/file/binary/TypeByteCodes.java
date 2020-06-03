package org.dddjava.jig.domain.model.jigsource.file.binary;

import org.dddjava.jig.domain.model.jigmodel.declaration.annotation.*;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.Type;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.Types;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * モデルの実装一式
 */
public class TypeByteCodes {
    private final List<TypeByteCode> list;

    public TypeByteCodes(List<TypeByteCode> list) {
        this.list = list;
    }

    public List<TypeByteCode> list() {
        return list;
    }

    public List<MethodByteCode> instanceMethodByteCodes() {
        return list.stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .collect(toList());
    }

    public TypeAnnotations typeAnnotations() {
        List<TypeAnnotation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            list.addAll(typeByteCode.typeAnnotations());
        }
        return new TypeAnnotations(list);
    }

    public FieldAnnotations annotatedFields() {
        List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            fieldAnnotations.addAll(typeByteCode.annotatedFields());
        }
        return new FieldAnnotations(fieldAnnotations);
    }

    public MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = new ArrayList<>();
        for (MethodByteCode methodByteCode : instanceMethodByteCodes()) {
            methodAnnotations.addAll(methodByteCode.annotatedMethods().list());
        }
        return new MethodAnnotations(methodAnnotations);
    }

    public FieldDeclarations instanceFields() {
        List<FieldDeclaration> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            FieldDeclarations fieldDeclarations = typeByteCode.fieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public StaticFieldDeclarations staticFields() {
        List<StaticFieldDeclaration> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            StaticFieldDeclarations fieldDeclarations = typeByteCode.staticFieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new StaticFieldDeclarations(list);
    }

    public Types types() {
        List<Type> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            list.add(typeByteCode.type());
        }
        return new Types(list);
    }

    public ValidationAnnotatedMembers validationAnnotatedMembers() {
        return new ValidationAnnotatedMembers(annotatedFields(), annotatedMethods());
    }
}
