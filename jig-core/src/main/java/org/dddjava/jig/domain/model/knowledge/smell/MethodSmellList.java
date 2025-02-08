package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.type.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellList {
    List<MethodSmell> list;

    public MethodSmellList(JigTypes jigTypes) {
        this.list = jigTypes.stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream()
                        .flatMap(method -> MethodSmell.createMethodSmell(method, jigType.instanceJigFields().fieldDeclarations()).stream())
                )
                .collect(Collectors.toList());
    }

    public List<MethodSmell> list() {
        return list.stream()
                .sorted(Comparator.comparing(methodSmell -> methodSmell.methodDeclaration().asFullNameText()))
                .collect(Collectors.toList());
    }

    public List<MethodSmell> collectBy(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(methodSmell -> methodSmell.methodDeclaration().declaringType().equals(typeIdentifier))
                .toList();
    }
}
