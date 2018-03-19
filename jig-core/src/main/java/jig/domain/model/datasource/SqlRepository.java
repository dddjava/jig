package jig.domain.model.datasource;

import jig.domain.model.thing.Name;

import java.util.Optional;

public interface SqlRepository {

    Optional<Sql> find(Name name);

    void register(Sql sql);

    Sql get(SqlIdentifier identifier);
}
