package jig.domain.model.datasource;

import jig.domain.model.identifier.Identifier;

import java.util.Optional;

public interface SqlRepository {

    Optional<Sql> find(Identifier identifier);

    void register(Sql sql);

    Sql get(SqlIdentifier identifier);
}
