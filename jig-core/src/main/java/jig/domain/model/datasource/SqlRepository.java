package jig.domain.model.datasource;

import jig.domain.model.thing.Identifier;

import java.util.Optional;

public interface SqlRepository {

    Optional<Sql> find(Identifier identifier);

    void register(Sql sql);

    Sql get(SqlIdentifier identifier);
}
