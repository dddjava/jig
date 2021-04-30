package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethods;
import org.dddjava.jig.domain.model.parts.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * インスタンスに属するもの
 */
public class JigInstanceMember {
    FieldDeclarations fieldDeclarations;
    JigMethods instanceMethods;

    public JigInstanceMember(FieldDeclarations fieldDeclarations, JigMethods instanceMethods) {
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

    public JigMethods instanceMethods() {
        return instanceMethods;
    }

    List<TypeIdentifier> listUsingTypes() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.addAll(fieldDeclarations.toTypeIdentifies().list());
        list.addAll(instanceMethods.listUsingTypes());
        return list;
    }
}
