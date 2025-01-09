package org.dddjava.jig.domain.model.data.classes.rdbaccess;

/**
 * SQL
 */
public class MyBatisStatement {

    MyBatisStatementId identifier;
    Query query;
    SqlType sqlType;

    public MyBatisStatement(MyBatisStatementId identifier, Query query, SqlType sqlType) {
        this.identifier = identifier;
        this.query = query;
        this.sqlType = sqlType;
    }

    public SqlType sqlType() {
        return sqlType;
    }

    public Tables tables() {
        return query.extractTable(sqlType);

    }

    public MyBatisStatementId identifier() {
        return identifier;
    }
}
