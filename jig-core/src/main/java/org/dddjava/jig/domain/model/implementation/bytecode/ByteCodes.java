package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.*;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.declaration.method.Methods;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
                .collect(toList());
    }

    public Methods instanceMethods() {
        List<Method> list = instanceMethodByteCodes().stream()
                .map(MethodByteCode::method)
                .collect(toList());
        return new Methods(list);
    }

    public TypeAnnotations typeAnnotations() {
        List<TypeAnnotation> list = new ArrayList<>();
        for (ByteCode byteCode : list()) {
            list.addAll(byteCode.typeAnnotations());
        }
        return new TypeAnnotations(list);
    }

    public FieldAnnotations annotatedFields() {
        List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
        for (ByteCode byteCode : list()) {
            fieldAnnotations.addAll(byteCode.annotatedFields());
        }
        return new FieldAnnotations(fieldAnnotations);
    }

    public MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = new ArrayList<>();
        for (MethodByteCode methodByteCode : instanceMethodByteCodes()) {
            methodAnnotations.addAll(methodByteCode.annotatedMethods());
        }
        return new MethodAnnotations(methodAnnotations);
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
