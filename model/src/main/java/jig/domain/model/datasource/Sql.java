package jig.domain.model.datasource;

public class Sql {

    SqlIdentifier identifier;
    Query query;
    SqlType sqlType;

    public Sql(SqlIdentifier identifier, Query query, SqlType sqlType) {
        this.identifier = identifier;
        this.query = query;
        this.sqlType = sqlType;
    }

    public SqlType sqlType() {
        return sqlType;
    }

    public String tableName() {
        return query.extractTable(sqlType);

    }

    public SqlIdentifier identifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "Sql{" +
                "identifier=" + identifier +
                ", query=" + query +
                ", sqlType=" + sqlType +
                '}';
    }
}
