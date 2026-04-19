package org.dddjava.jig.domain.model.data.persistence;

import java.util.Collection;
import java.util.Collections;

/**
 * 永続化操作対象群
 */
public record PersistenceTargetOperationTypes(Collection<PersistenceTargetOperationType> persistenceTargets) {

    public PersistenceTargetOperationTypes {
        // if (persistenceTargets.isEmpty()) throw new IllegalArgumentException("永続化対象は1件以上である必要があります");
        // ...にしたいのだけど、出力時にnothing()を使用しているので今のところできない。永続化周りの出力が片付いたらできるようになるはず。
    }

    public PersistenceTargetOperationTypes(PersistenceTargetOperationType persistenceTarget) {
        this(Collections.singletonList(persistenceTarget));
    }
}
