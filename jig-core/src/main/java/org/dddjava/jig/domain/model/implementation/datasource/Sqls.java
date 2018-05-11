package org.dddjava.jig.domain.model.implementation.datasource;

import java.util.List;

public class Sqls {

    List<Sql> list;

    public Sqls(List<Sql> list) {
        this.list = list;
    }

    public Tables tables(SqlType sqlType) {
        return list.stream()
                .filter(sql -> sql.sqlType() == sqlType)
                .map(Sql::tables)
                .reduce(Tables::merge)
                .orElse(Tables.nothing());
    }

    public List<Sql> list() {
        return list;
    }
}
