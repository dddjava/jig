package org.dddjava.jig.domain.model.data.rdbaccess;

/**
 * SQL
 */
public record MyBatisStatement(MyBatisStatementId myBatisStatementId, Query query, SqlType sqlType) {

    public Tables tables() {
        if (query.supported()) {
            Table table = sqlType.extractTable(query.text(), myBatisStatementId);
            return new Tables(table);
        }
        return new Tables(sqlType.unexpectedTable());
    }
}
