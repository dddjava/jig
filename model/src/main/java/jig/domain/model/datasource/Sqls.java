package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

import java.util.List;
import java.util.NoSuchElementException;

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
}
