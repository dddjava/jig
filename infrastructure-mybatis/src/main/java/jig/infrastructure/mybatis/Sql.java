package jig.infrastructure.mybatis;

public class Sql {

    SqlIdentifier identifier;
    String sql;
    SqlType sqlType;

    public Sql(SqlIdentifier identifier, String sql, SqlType sqlType) {
        this.identifier = identifier;
        this.sql = sql;
        this.sqlType = sqlType;
    }
}
