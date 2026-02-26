package org.dddjava.jig.domain.model.data.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.joining;

/**
 * 永続化操作対象群
 */
public record PersistenceTargets(Collection<PersistenceTarget> persistenceTargets) {

    public PersistenceTargets(PersistenceTarget persistenceTarget) {
        this(Collections.singletonList(persistenceTarget));
    }

    public static PersistenceTargets nothing() {
        return new PersistenceTargets(Collections.emptyList());
    }

    public PersistenceTargets merge(PersistenceTargets other) {
        ArrayList<PersistenceTarget> list = new ArrayList<>(this.persistenceTargets);
        list.addAll(other.persistenceTargets);
        return new PersistenceTargets(list);
    }

    public String asText() {
        // 文字列としてユニーク。ソートされてるのは自然なのでメソッド名に含めない。
        return persistenceTargets.stream()
                .map(PersistenceTarget::name)
                .distinct()
                .sorted()
                .collect(joining(", ", "[", "]"));
    }
}
