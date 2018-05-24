package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.implementation.datasource.Sql;
import org.dddjava.jig.domain.model.implementation.datasource.SqlRepository;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OnMemorySqlRepository implements SqlRepository {

    List<Sql> list = new ArrayList<>();

    @Override
    public void register(Sql sql) {
        list.add(sql);
    }

    @Override
    public Sqls all() {
        return new Sqls(list);
    }
}
