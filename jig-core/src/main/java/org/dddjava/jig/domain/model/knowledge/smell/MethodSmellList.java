package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public class MethodSmellList {
    List<MethodSmell> list;

    public MethodSmellList(JigTypes jigTypes, MethodRelations methodRelations) {
        this.list = jigTypes.list().stream()
                .flatMap(jigType -> jigType.instanceMember().instanceMethods().list().stream()
                        .flatMap(method -> MethodSmell.createMethodSmell(method, jigType.instanceMember().fieldDeclarations(), methodRelations).stream())
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
