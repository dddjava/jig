package org.dddjava.jig.domain.model.jigmodel.jigtype;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;

public class JigInstanceMember {
    private final FieldDeclarations fieldDeclarations;
    private final MethodDeclarations instanceMethodDeclarations;

    public JigInstanceMember(FieldDeclarations fieldDeclarations, MethodDeclarations instanceMethodDeclarations) {
        this.fieldDeclarations = fieldDeclarations;
        this.instanceMethodDeclarations = instanceMethodDeclarations;
    }

    public FieldDeclarations getFieldDeclarations() {
        return fieldDeclarations;
    }

    public MethodDeclarations getInstanceMethodDeclarations() {
        return instanceMethodDeclarations;
    }

    public boolean hasField() {
        return !fieldDeclarations.empty();
    }

    public boolean hasMethod() {
        return !instanceMethodDeclarations.empty();
    }
}
