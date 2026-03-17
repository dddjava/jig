package org.dddjava.jig.domain.model.data.persistence;

/**
 * 永続化アクセサ操作
 *
 * 操作内容（CRUD）や操作対象（テーブルなど）を持つ。
 * クエリはわかる場合のみ持つ。
 */
public record PersistenceAccessorOperation(PersistenceAccessorOperationId persistenceAccessorOperationId,
                                           Query query,
                                           PersistenceOperationType persistenceOperationType,
                                           PersistenceTargets persistenceTargets) {

    public static PersistenceAccessorOperation from(PersistenceAccessorOperationId persistenceAccessorOperationId, Query query, PersistenceOperationType persistenceOperationType) {
        return new PersistenceAccessorOperation(persistenceAccessorOperationId, query, persistenceOperationType, persistenceOperationType.extractTable(query, persistenceAccessorOperationId));
    }

    public static PersistenceAccessorOperation from(PersistenceAccessorOperationId statementId, PersistenceOperationType persistenceOperationType, PersistenceTargets persistenceTargets) {
        // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
        return new PersistenceAccessorOperation(statementId, Query.unsupported(), persistenceOperationType, persistenceTargets);
    }
}
