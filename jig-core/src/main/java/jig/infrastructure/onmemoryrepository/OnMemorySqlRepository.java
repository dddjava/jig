package jig.infrastructure.onmemoryrepository;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.thing.Name;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class OnMemorySqlRepository implements SqlRepository {

    List<Sql> list = new ArrayList<>();

    @Override
    public Sql get(Name name) {
        return list.stream()
                .filter(s -> s.name().equals(name))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    @Override
    public void register(Sql sql) {
        list.add(sql);
    }
}
