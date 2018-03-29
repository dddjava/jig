package jig.domain.model.datasource;

import jig.domain.model.identifier.method.MethodIdentifier;

import java.util.Optional;

public interface SqlRepository {

    Optional<Sql> find(MethodIdentifier identifier);

    void register(Sql sql);

    default void register(Sqls sqls) {
        sqls.list().forEach(this::register);
    }

    Sql get(SqlIdentifier identifier);
}
