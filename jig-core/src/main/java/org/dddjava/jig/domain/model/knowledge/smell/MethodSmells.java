package org.dddjava.jig.domain.model.knowledge.smell;

import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * メソッドの不吉なにおい一覧
 */
public record MethodSmells(Collection<MethodSmell> smells) {

    public static MethodSmells from(JigTypes jigTypes) {
        return new MethodSmells(jigTypes.orderedStream()
                .flatMap(jigType -> jigType.instanceJigMethodStream()
                        .flatMap(method -> MethodSmell.from(method, jigType).stream())
                )
                .toList());
    }

    public List<MethodSmell> list() {
        return smells.stream()
                .sorted(Comparator.comparing(methodSmell -> methodSmell.method().jigMethodId().value()))
                .toList();
    }
}
