package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.datasource.Sql;
import org.dddjava.jig.domain.model.datasource.SqlIdentifier;
import org.dddjava.jig.domain.model.datasource.SqlRepository;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class OnMemorySqlRepository implements SqlRepository {

    Map<SqlIdentifier, Sql> map = new HashMap<>();

    @Override
    public Optional<Sql> find(MethodDeclaration identifier) {
        return map.keySet().stream()
                .filter(i -> i.matches(identifier))
                .findFirst()
                .map(map::get);
    }

    @Override
    public void register(Sql sql) {
        map.put(sql.identifier(), sql);
    }
}
