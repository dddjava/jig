package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedField;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedFields;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethod;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethods;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * モデルの実装一式
 */
public class ByteCodes {
    private final List<ByteCode> list;

    public ByteCodes(List<ByteCode> list) {
        this.list = list;
    }

    public List<ByteCode> list() {
        return list;
    }

    public List<MethodByteCode> instanceMethodByteCodes() {
        return list.stream()
                .map(ByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public AnnotatedFields annotatedFields() {
        List<AnnotatedField> annotatedFields = new ArrayList<>();
        for (ByteCode byteCode : list()) {
            annotatedFields.addAll(byteCode.fieldAnnotationDeclarations());
        }
        return new AnnotatedFields(annotatedFields);
    }

    public AnnotatedMethods annotatedMethods() {
        List<AnnotatedMethod> annotatedMethods = new ArrayList<>();
        for (MethodByteCode methodByteCode : instanceMethodByteCodes()) {
            annotatedMethods.addAll(methodByteCode.methodAnnotationDeclarations());
        }
        return new AnnotatedMethods(annotatedMethods);
    }

    public FieldDeclarations instanceFields() {
        List<FieldDeclaration> list = new ArrayList<>();
        for (ByteCode byteCode : list()) {
            FieldDeclarations fieldDeclarations = byteCode.fieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public StaticFieldDeclarations staticFields() {
        List<StaticFieldDeclaration> list = new ArrayList<>();
        for (ByteCode byteCode : list()) {
            StaticFieldDeclarations fieldDeclarations = byteCode.staticFieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new StaticFieldDeclarations(list);
    }
}
