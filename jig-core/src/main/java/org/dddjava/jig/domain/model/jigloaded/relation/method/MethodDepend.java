package org.dddjava.jig.domain.model.jigloaded.relation.method;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * メソッドが依存しているもの
 */
public class MethodDepend {

    Set<TypeIdentifier> usingTypes;
    List<FieldDeclaration> usingFields;
    List<MethodDeclaration> usingMethods;
    boolean hasNullReference;

    public MethodDepend(Set<TypeIdentifier> usingTypes, List<FieldDeclaration> usingFields, List<MethodDeclaration> usingMethods, boolean hasNullReference) {
        this.usingTypes = usingTypes;
        this.usingFields = usingFields;
        this.usingMethods = usingMethods;
        this.hasNullReference = hasNullReference;
    }

    public UsingFields usingFields() {
        FieldDeclarations fieldDeclarations = usingFields.stream().collect(FieldDeclarations.collector());
        return new UsingFields(fieldDeclarations);
    }

    public UsingMethods usingMethods() {
        return new UsingMethods(usingMethods.stream().collect(MethodDeclarations.collector()));
    }

    public boolean notUseMember() {
        return usingFields.isEmpty() && usingMethods.isEmpty();
    }

    public boolean hasNullReference() {
        return hasNullReference;
    }

    public Collection<TypeIdentifier> useTypes() {
        Set<TypeIdentifier> typeIdentifiers = new HashSet<>(usingTypes);

        for (FieldDeclaration usingField : usingFields) {
            typeIdentifiers.add(usingField.declaringType());
            typeIdentifiers.add(usingField.typeIdentifier());
        }

        for (MethodDeclaration usingMethod : usingMethods) {
        }

        return typeIdentifiers;
    }
}
