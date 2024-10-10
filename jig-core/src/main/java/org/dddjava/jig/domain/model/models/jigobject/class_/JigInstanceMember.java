package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.ArrayList;
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
        List<TypeIdentifier> list = new ArrayList<>();
        list.addAll(instanceFields.listUsingTypes());
        list.addAll(instanceMethods.listUsingTypes());
        return list;
    }

    public JigFields instanceFields() {
        return instanceFields;
    }

    public Stream<JigMethod> jigMethodStream() {
        return instanceMethods.stream();
    }
}
