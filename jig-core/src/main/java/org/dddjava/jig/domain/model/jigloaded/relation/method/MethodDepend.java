package org.dddjava.jig.domain.model.jigloaded.relation.method;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.Set;

/**
 * メソッドが依存しているもの
 */
public class MethodDepend {

    Set<TypeIdentifier> usingTypes;
    FieldDeclarations usingFields;
    MethodDeclarations usingMethods;

    public MethodDepend(Set<TypeIdentifier> usingTypes, FieldDeclarations usingFields, MethodDeclarations usingMethods) {
        this.usingTypes = usingTypes;
        this.usingFields = usingFields;
        this.usingMethods = usingMethods;
    }

    public UsingFields usingFields() {
        return new UsingFields(usingFields);
    }

    public UsingMethods usingMethods() {
        return new UsingMethods(usingMethods);
    }
}
