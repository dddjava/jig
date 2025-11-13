package org.dddjava.jig.domain.model.data.rdbaccess;

/**
 * SQL
 */
public record MyBatisStatement(MyBatisStatementId myBatisStatementId, Query query, SqlType sqlType) {

    public Tables tables() {
        return query.extractTable(sqlType);
    }
}
