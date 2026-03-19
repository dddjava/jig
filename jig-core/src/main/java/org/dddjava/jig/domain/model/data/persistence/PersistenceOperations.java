package org.dddjava.jig.domain.model.data.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.joining;

/**
 * 永続化操作対象群
 */
public record PersistenceOperations(Collection<PersistenceOperation> persistenceTargets) {

    public PersistenceOperations {
        // if (persistenceTargets.isEmpty()) throw new IllegalArgumentException("永続化対象は1件以上である必要があります");
        // ...にしたいのだけど、出力時にnothing()を使用しているので今のところできない。永続化周りの出力が片付いたらできるようになるはず。
    }

    public PersistenceOperations(PersistenceOperation persistenceTarget) {
        this(Collections.singletonList(persistenceTarget));
    }

    public static PersistenceOperations nothing() {
        return new PersistenceOperations(Collections.emptyList());
    }

    // TODO テストでのみ使用している。テストにもっていくか、テストを見直してなくす。
    public PersistenceOperations merge(PersistenceOperations other) {
        ArrayList<PersistenceOperation> list = new ArrayList<>(this.persistenceTargets);
        list.addAll(other.persistenceTargets);
        return new PersistenceOperations(list);
    }

    // TODO テストでのみ使用している。テストにもっていくか、テストを見直してなくす。
    public String asText() {
        // 文字列としてユニーク。ソートされてるのは自然なのでメソッド名に含めない。
        return persistenceTargets.stream()
                .map(PersistenceOperation::persistenceTarget)
                .map(PersistenceTarget::name)
                .distinct()
                .sorted()
                .collect(joining(", ", "[", "]"));
    }
}
