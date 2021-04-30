package org.dddjava.jig.domain.model.parts.declaration.field;

import org.dddjava.jig.domain.model.parts.declaration.text.Text;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifiers;

import java.util.Arrays;
import java.util.List;

/**
 * フィールド定義一覧
 */
public class FieldDeclarations {

    List<FieldDeclaration> list;

    public FieldDeclarations(List<FieldDeclaration> list) {
        this.list = list;
    }

    @Deprecated
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

    public FieldDeclaration onlyOneField() {
        // TODO 0個 or
        return list.get(0);
    }

    public boolean empty() {
        return list.isEmpty();
    }
}
