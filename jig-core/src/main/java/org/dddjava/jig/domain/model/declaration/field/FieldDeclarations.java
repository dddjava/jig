package org.dddjava.jig.domain.model.declaration.field;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FieldDeclarations {

    List<FieldDeclaration> list;

    public FieldDeclarations(List<FieldDeclaration> list) {
        this.list = list;
    }

    public static Collector<FieldDeclaration, ?, FieldDeclarations> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), FieldDeclarations::new);
    }

    public static FieldDeclarations empty() {
        return new FieldDeclarations(Collections.emptyList());
    }

    public static FieldDeclarations ofInstanceField(Implementations implementations) {
        List<FieldDeclaration> list = new ArrayList<>();
        for (Implementation implementation : implementations.list()) {
            FieldDeclarations fieldDeclarations = implementation.fieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public static FieldDeclarations ofStaticField(Implementations implementations) {
        List<FieldDeclaration> list = new ArrayList<>();
        for (Implementation implementation : implementations.list()) {
            FieldDeclarations fieldDeclarations = implementation.staticFieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public List<FieldDeclaration> list() {
        return list;
    }

    public String toNameText() {
        return list.stream()
                .map(FieldDeclaration::nameText)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String toSignatureText() {
        return list.stream()
                .map(FieldDeclaration::signatureText)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public TypeIdentifiers toTypeIdentifies() {
        return list.stream()
                .map(FieldDeclaration::typeIdentifier)
                .distinct()
                .collect(TypeIdentifiers.collector());
    }

    public boolean matches(TypeIdentifier... typeIdentifiers) {
        if (list.size() != typeIdentifiers.length) return false;
        return Arrays.equals(typeIdentifiers,
                list.stream().map(FieldDeclaration::typeIdentifier).toArray(TypeIdentifier[]::new));
    }

    public FieldDeclarations filterDeclareTypeIs(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(fieldDeclaration -> fieldDeclaration.declaringType().equals(typeIdentifier))
                .collect(FieldDeclarations.collector());
    }
}
