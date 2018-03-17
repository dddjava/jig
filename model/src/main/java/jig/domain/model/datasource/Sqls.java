package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
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

    public String crud() {
        Map<String, Set<SqlType>> collected = list.stream()
                .collect(Collectors.groupingBy(Sql::tableName,
                        Collectors.mapping(
                                Sql::sqlType,
                                Collectors.toSet())));
        return collected.toString();
    }
}
