package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドの不吉なにおい一覧
 */
public record MethodSmellList(JigTypes jigTypes, List<MethodSmell> list) {

    public static MethodSmellList from(JigTypes jigTypes) {
        return new MethodSmellList(jigTypes, jigTypes.stream()
                .flatMap(jigType -> jigType.instanceJigMethodStream()
                        .flatMap(method -> MethodSmell.createMethodSmell(method, jigType).stream())
                )
                .collect(Collectors.toList()));
    }

    public List<MethodSmell> list() {
        return list.stream()
                .sorted(Comparator.comparing(methodSmell -> methodSmell.method().jigMethodIdentifier().value()))
                .collect(Collectors.toList());
    }
}
