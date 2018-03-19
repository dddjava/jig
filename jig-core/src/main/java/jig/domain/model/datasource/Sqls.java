package jig.domain.model.datasource;

import java.util.List;
import java.util.stream.Collectors;

public class Sqls {

    List<Sql> list;

    public Sqls(List<Sql> list) {
        this.list = list;
    }

    public String tables(SqlType sqlType) {
        return list.stream()
                .filter(sql -> sql.sqlType() == sqlType)
                .map(Sql::tableName)
                .collect(Collectors.joining("/"));
    }
}
