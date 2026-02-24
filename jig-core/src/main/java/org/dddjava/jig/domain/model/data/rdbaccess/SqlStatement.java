package org.dddjava.jig.domain.model.data.rdbaccess;

/**
 * SQL
 */
public record SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType, Tables tables) {

    public static SqlStatement from(SqlStatementId sqlStatementId, Query query, SqlType sqlType) {
        return new SqlStatement(sqlStatementId, query, sqlType, sqlType.extractTable(query, sqlStatementId));
    }

    public static SqlStatement from(SqlStatementId statementId, SqlType sqlType, Tables tables) {
        // TODO: Queryはunsupportedではなくauto-generateとかそんな感じかと思う
        return new SqlStatement(statementId, Query.unsupported(), sqlType, tables);
    }
}
