package jig.infrastructure;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class OnMemorySqlRepository implements SqlRepository {

    List<Sql> list = new ArrayList<>();

    @Override
    public Sql get(SqlIdentifier sqlIdentifier) {
        return list.stream()
                .filter(s -> s.identifier().equals(sqlIdentifier))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void register(Sql sql) {
        list.add(sql);
    }
}
