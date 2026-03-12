package org.dddjava.jig.domain.model.data.persistence;

/**
 * 永続化アクセサ
 *
 * 操作内容（CRUD）や操作対象（テーブルなど）を持つ。
 * クエリはわかる場合のみ持つ。
 */
public record PersistenceAccessor(PersistenceAccessorId persistenceAccessorId, Query query, SqlType sqlType, PersistenceTargets persistenceTargets) {

    public static PersistenceAccessor from(PersistenceAccessorId persistenceAccessorId, Query query, SqlType sqlType) {
        return new PersistenceAccessor(persistenceAccessorId, query, sqlType, sqlType.extractTable(query, persistenceAccessorId));
    }

    public static PersistenceAccessor from(PersistenceAccessorId statementId, SqlType sqlType, PersistenceTargets persistenceTargets) {
        // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
        return new PersistenceAccessor(statementId, Query.unsupported(), sqlType, persistenceTargets);
    }
}
