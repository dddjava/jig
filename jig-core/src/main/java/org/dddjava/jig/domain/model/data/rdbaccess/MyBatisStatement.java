package org.dddjava.jig.domain.model.data.rdbaccess;

/**
 * SQL
 */
public class MyBatisStatement {

    MyBatisStatementId myBatisStatementId;
    Query query;
    SqlType sqlType;

    public MyBatisStatement(MyBatisStatementId myBatisStatementId, Query query, SqlType sqlType) {
        this.myBatisStatementId = myBatisStatementId;
        this.query = query;
        this.sqlType = sqlType;
    }

    public SqlType sqlType() {
        return sqlType;
    }

    public Tables tables() {
        return query.extractTable(sqlType);

    }

    public MyBatisStatementId myBatisStatementId() {
        return myBatisStatementId;
    }
}
