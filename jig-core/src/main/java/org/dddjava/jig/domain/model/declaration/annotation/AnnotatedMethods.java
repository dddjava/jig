package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedMethods {
    private final ByteCodes byteCodes;

    public AnnotatedMethods(ByteCodes byteCodes) {
        this.byteCodes = byteCodes;
    }

    public List<AnnotatedField> list() {
        List<AnnotatedField> annotatedFields = new ArrayList<>();
        for (ByteCode byteCode : byteCodes.list()) {
            annotatedFields.addAll(byteCode.fieldAnnotationDeclarations());
        }
        return annotatedFields;
    }
}
