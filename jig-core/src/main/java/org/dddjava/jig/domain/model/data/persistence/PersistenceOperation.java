package org.dddjava.jig.domain.model.data.persistence;

/**
 * 永続化操作
 *
 * 操作内容（CRUD）や操作対象（テーブルなど）を持つ。
 * クエリはわかる場合のみ持つ。
 */
public record PersistenceOperation(PersistenceOperationId persistenceOperationId, Query query, SqlType sqlType, PersistenceTargets persistenceTargets) {

    public static PersistenceOperation from(PersistenceOperationId persistenceOperationId, Query query, SqlType sqlType) {
        return new PersistenceOperation(persistenceOperationId, query, sqlType, sqlType.extractTable(query, persistenceOperationId));
    }

    public static PersistenceOperation from(PersistenceOperationId statementId, SqlType sqlType, PersistenceTargets persistenceTargets) {
        // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
        return new PersistenceOperation(statementId, Query.unsupported(), sqlType, persistenceTargets);
    }
}
