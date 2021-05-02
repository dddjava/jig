package org.dddjava.jig.domain.model.models.jigobject.class_;

import org.dddjava.jig.domain.model.models.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.class_.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * インスタンスに属するもの
 */
public class JigInstanceMember {
    JigFields jigFields;
    JigMethods instanceMethods;

    public JigInstanceMember(JigFields jigFields, JigMethods instanceMethods) {
        this.jigFields = jigFields;
        this.instanceMethods = instanceMethods;
    }

    public boolean hasField() {
        return !jigFields.empty();
    }

    public FieldDeclarations fieldDeclarations() {
        return jigFields.fieldDeclarations();
    }

    public boolean hasMethod() {
        return !instanceMethods.empty();
    }

    public JigMethods instanceMethods() {
        return instanceMethods;
    }

    List<TypeIdentifier> listUsingTypes() {
        List<TypeIdentifier> list = new ArrayList<>();
        list.addAll(jigFields.typeIdentifies().list());
        list.addAll(instanceMethods.listUsingTypes());
        return list;
    }
}
