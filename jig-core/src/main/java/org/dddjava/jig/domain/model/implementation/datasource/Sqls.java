package org.dddjava.jig.domain.model.implementation.datasource;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

import java.util.List;
import java.util.stream.Collectors;

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

    public Sqls filterRelationOn(MethodDeclarations methodDeclarations) {
        List<Sql> sqls = list.stream()
                .filter(sql -> sql.identifier().matches(methodDeclarations))
                .collect(Collectors.toList());
        return new Sqls(sqls);
    }
}
