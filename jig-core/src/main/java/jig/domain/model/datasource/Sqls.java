package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Sqls {

    List<Sql> list;

    public Sqls(List<Sql> list) {
        this.list = list;
    }

    public Sql get(Name name) {
        return list.stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    public String tables(SqlType sqlType) {
        return list.stream()
                .filter(sql -> sql.sqlType() == sqlType)
                .map(Sql::tableName)
                .collect(Collectors.joining("/"));
    }
}
