package org.dddjava.jig.domain.model.data.rdbaccess;

/**
 * 永続化操作
 *
 * 操作内容（CRUD）や操作対象（テーブルなど）を持つ。
 * クエリはわかる場合のみ持つ。
 */
public record PersistenceOperation(SqlStatementId sqlStatementId, Query query, SqlType sqlType, Tables tables) {

    public static PersistenceOperation from(SqlStatementId sqlStatementId, Query query, SqlType sqlType) {
        return new PersistenceOperation(sqlStatementId, query, sqlType, sqlType.extractTable(query, sqlStatementId));
    }

    public static PersistenceOperation from(SqlStatementId statementId, SqlType sqlType, Tables tables) {
        // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
        return new PersistenceOperation(statementId, Query.unsupported(), sqlType, tables);
    }
}
