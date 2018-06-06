package org.dddjava.jig.domain.model.declaration.field;

import org.dddjava.jig.domain.basic.Text;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * staticフィールド定義一覧
 */
public class StaticFieldDeclarations {

    List<StaticFieldDeclaration> list;

    public StaticFieldDeclarations(List<StaticFieldDeclaration> list) {
        this.list = list;
    }

    public static Collector<StaticFieldDeclaration, ?, StaticFieldDeclarations> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), StaticFieldDeclarations::new);
    }

    public List<StaticFieldDeclaration> list() {
        return list;
    }

    public String toNameText() {
        return list.stream()
                .map(StaticFieldDeclaration::nameText)
                .collect(Text.collectionCollector());
    }

    public StaticFieldDeclarations filterDeclareTypeIs(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(fieldDeclaration -> fieldDeclaration.declaringType().equals(typeIdentifier))
                .collect(StaticFieldDeclarations.collector());
    }
}
