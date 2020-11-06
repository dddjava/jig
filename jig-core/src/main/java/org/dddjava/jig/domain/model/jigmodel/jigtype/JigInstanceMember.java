package org.dddjava.jig.domain.model.jigmodel.jigtype;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;

/**
 * インスタンスに属するもの
 */
public class JigInstanceMember {
    FieldDeclarations fieldDeclarations;
    Methods instanceMethods;

    public JigInstanceMember(FieldDeclarations fieldDeclarations, Methods instanceMethods) {
        this.fieldDeclarations = fieldDeclarations;
        this.instanceMethods = instanceMethods;
    }

    public boolean hasField() {
        return !fieldDeclarations.empty();
    }

    public FieldDeclarations fieldDeclarations() {
        return fieldDeclarations;
    }

    public boolean hasMethod() {
        return !instanceMethods.empty();
    }

    public Methods instanceMethods() {
        return instanceMethods;
    }
}
