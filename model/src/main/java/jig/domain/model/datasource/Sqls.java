package jig.domain.model.datasource;

import java.util.List;
import java.util.NoSuchElementException;

public class Sqls {

    List<Sql> list;

    public Sqls(List<Sql> list) {
        this.list = list;
    }

    public Sql get(SqlIdentifier sqlIdentifier) {
        return list.stream()
                .filter(s -> s.identifier.equals(sqlIdentifier))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }
}
