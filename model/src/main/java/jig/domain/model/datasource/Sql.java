package jig.domain.model.datasource;

public class Sql {

    SqlIdentifier identifier;
    String sql;
    SqlType sqlType;

    public Sql(SqlIdentifier identifier, String sql, SqlType sqlType) {
        this.identifier = identifier;
        this.sql = sql;
        this.sqlType = sqlType;
    }

    public SqlType sqlType() {
        return sqlType;
    }

    public String tableName() {
        return sqlType.extractTable(sql.replaceAll("\n", " "));

    }

    public SqlIdentifier identifier() {
        return identifier;
    }
}
