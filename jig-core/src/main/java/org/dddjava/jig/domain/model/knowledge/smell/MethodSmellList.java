package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.information.types.JigTypes;

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
                .flatMap(jigType -> jigType.instanceJigMethodStream()
                        .flatMap(method -> MethodSmell.createMethodSmell(method, jigType).stream())
                )
                .collect(Collectors.toList());
    }

    public List<MethodSmell> list() {
        return list.stream()
                .sorted(Comparator.comparing(methodSmell -> methodSmell.method().jigMethodIdentifier().value()))
                .collect(Collectors.toList());
    }
}
