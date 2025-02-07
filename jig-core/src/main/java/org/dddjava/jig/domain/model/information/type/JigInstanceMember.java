package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Stream;

/**
 * インスタンスに属するもの
 */
public class JigInstanceMember {
    private final JigFields instanceFields;
    private final JigMethods instanceMethods;

    public JigInstanceMember(JigFields instanceFields, JigMethods instanceMethods) {
        this.instanceFields = instanceFields;
        this.instanceMethods = instanceMethods;
    }

    public boolean hasField() {
        return !instanceFields.empty();
    }

    public FieldDeclarations fieldDeclarations() {
        return instanceFields.fieldDeclarations();
    }

    public boolean hasMethod() {
        return !instanceMethods.empty();
    }

    public JigMethods instanceMethods() {
        return instanceMethods;
    }

    List<TypeIdentifier> listUsingTypes() {
        return instanceMethods.listUsingTypes();
    }

    public JigFields instanceFields() {
        return instanceFields;
    }

    public Stream<JigMethod> jigMethodStream() {
        return instanceMethods.stream();
    }
}
