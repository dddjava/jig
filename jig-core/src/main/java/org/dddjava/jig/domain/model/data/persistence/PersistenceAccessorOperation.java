package org.dddjava.jig.domain.model.data.persistence;

import java.util.Collection;

/**
 * 永続化アクセサ操作
 *
 * 操作内容（CRUD）や操作対象（テーブルなど）を持つ。
 * クエリはわかる場合のみ持つ。
 */
public record PersistenceAccessorOperation(PersistenceAccessorOperationId persistenceAccessorOperationId,
                                           Query query,
                                           PersistenceOperationType persistenceOperationType,
                                           PersistenceOperations persistenceOperations) {

    public static PersistenceAccessorOperation from(PersistenceAccessorOperationId persistenceAccessorOperationId, Query query, PersistenceOperationType persistenceOperationType) {
        return new PersistenceAccessorOperation(persistenceAccessorOperationId, query, persistenceOperationType, persistenceOperationType.extractTable(query, persistenceAccessorOperationId));
    }

    public static PersistenceAccessorOperation from(PersistenceAccessorOperationId statementId, PersistenceOperationType persistenceOperationType, Collection<PersistenceTarget> persistenceTargets) {
        return new PersistenceAccessorOperation(
                statementId,
                // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
                Query.unsupported(),
                persistenceOperationType,
                new PersistenceOperations(persistenceTargets.stream()
                        .map(persistenceTarget -> PersistenceOperation.from(persistenceTarget, persistenceOperationType))
                        .toList()));
    }
}
