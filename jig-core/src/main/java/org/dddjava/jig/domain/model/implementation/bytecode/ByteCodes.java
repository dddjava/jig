package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedField;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedFields;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethod;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedMethods;

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

    public List<MethodByteCode> instanceMethodSpecifications() {
        return list.stream()
                .map(ByteCode::instanceMethodSpecifications)
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
        for (MethodByteCode methodSpecification : instanceMethodSpecifications()) {
            annotatedMethods.addAll(methodSpecification.methodAnnotationDeclarations());
        }
        return new AnnotatedMethods(annotatedMethods);
    }
}
