package jig.infrastructure.onmemoryrepository;

import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlIdentifier;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.identifier.Identifier;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class OnMemorySqlRepository implements SqlRepository {

    Map<SqlIdentifier, Sql> map = new HashMap<>();

    @Override
    public Optional<Sql> find(Identifier identifier) {
        return map.keySet().stream()
                .filter(i -> i.matches(identifier))
                .findFirst()
                .map(map::get);
    }

    @Override
    public Sql get(SqlIdentifier identifier) {
        return map.get(identifier);
    }

    @Override
    public void register(Sql sql) {
        map.put(sql.identifier(), sql);
    }
}
