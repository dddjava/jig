package org.dddjava.jig.domain.model.implementation.analyzed.declaration.field;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.type.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * フィールド定義一覧
 */
public class FieldDeclarations {

    List<FieldDeclaration> list;

    public FieldDeclarations(List<FieldDeclaration> list) {
        this.list = list;
    }

    public static Collector<FieldDeclaration, ?, FieldDeclarations> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), FieldDeclarations::new);
    }

    public List<FieldDeclaration> list() {
        return list;
    }

    public String toSignatureText() {
        return Text.of(list, FieldDeclaration::signatureText);
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
