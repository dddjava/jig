package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.List;
import java.util.stream.Collectors;

/**
 * staticフィールド定義一覧
 */
public class StaticFieldDeclarations {

    List<StaticFieldDeclaration> list;

    public StaticFieldDeclarations(List<StaticFieldDeclaration> list) {
        this.list = list;
    }

    public List<StaticFieldDeclaration> list() {
        return list;
    }

    public String toNameText() {
        return list.stream()
                .map(StaticFieldDeclaration::nameText)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public StaticFieldDeclarations selfDefineOnly() {
        return list.stream()
                .filter(StaticFieldDeclaration::isSelfDefine)
                .collect(Collectors.collectingAndThen(Collectors.toList(), StaticFieldDeclarations::new));
    }

    public List<TypeIdentifier> listTypeIdentifiers() {
        return list.stream()
                .map(staticFieldDeclaration -> staticFieldDeclaration.typeIdentifier())
                .collect(Collectors.toList());
    }
}
