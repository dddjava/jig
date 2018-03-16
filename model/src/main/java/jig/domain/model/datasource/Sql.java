package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

public class Sql {

    Name name;
    Query query;
    SqlType sqlType;

    public Sql(Name name, Query query, SqlType sqlType) {
        this.name = name;
        this.query = query;
        this.sqlType = sqlType;
    }

    public SqlType sqlType() {
        return sqlType;
    }

    public String tableName() {
        return query.extractTable(sqlType);

    }

    public Name name() {
        return name;
    }
}
