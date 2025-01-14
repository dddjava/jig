package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * フィールド定義一覧
 */
public class FieldDeclarations {

    List<FieldDeclaration> list;

    public FieldDeclarations(List<FieldDeclaration> list) {
        this.list = list;
    }

    public String toSignatureText() {
        return list.stream()
                .map(FieldDeclaration::signatureText)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public List<FieldDeclaration> list() {
        return list;
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

    public FieldDeclaration onlyOneField() {
        // TODO 0個 or
        return list.get(0);
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
