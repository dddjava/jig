package org.dddjava.jig.domain.model.data.rdbaccess;

/**
 * SQL
 */
public record SqlStatement(SqlStatementId sqlStatementId, Query query, SqlType sqlType) {

    public Tables tables() {
        if (query.supported()) {
            Table table = sqlType.extractTable(query.text(), sqlStatementId);
            return new Tables(table);
        }
        return new Tables(sqlType.unexpectedTable());
    }
}
