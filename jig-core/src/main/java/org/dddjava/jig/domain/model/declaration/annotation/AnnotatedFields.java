package org.dddjava.jig.domain.model.declaration.annotation;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;

import java.util.ArrayList;
import java.util.List;

public class AnnotatedFields {
    private final ByteCodes byteCodes;

    public AnnotatedFields(ByteCodes byteCodes) {
        this.byteCodes = byteCodes;
    }

    public List<MethodAnnotationDeclaration> list() {
        List<MethodAnnotationDeclaration> methodAnnotationDeclarations = new ArrayList<>();
        for (MethodImplementation methodSpecification : byteCodes.instanceMethodSpecifications()) {
            methodAnnotationDeclarations.addAll(methodSpecification.methodAnnotationDeclarations());
        }
        return methodAnnotationDeclarations;
    }
}
