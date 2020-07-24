package org.dddjava.jig.domain.model.jigmodel.jigtype;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;

public class JigInstanceMember {
    FieldDeclarations fieldDeclarations;
    Methods instanceMethods;

    public JigInstanceMember(FieldDeclarations fieldDeclarations, Methods instanceMethods) {
        this.fieldDeclarations = fieldDeclarations;
        this.instanceMethods = instanceMethods;
    }

    public FieldDeclarations getFieldDeclarations() {
        return fieldDeclarations;
    }

    public MethodDeclarations getInstanceMethodDeclarations() {
        return instanceMethods.declarations();
    }

    public boolean hasField() {
        return !fieldDeclarations.empty();
    }

    public boolean hasMethod() {
        return !instanceMethods.empty();
    }

    public Methods instanceMethods() {
        return instanceMethods;
    }
}
