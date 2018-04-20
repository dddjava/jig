package jig.domain.model.datasource;

import jig.domain.model.definition.method.MethodDefinition;

import java.util.Optional;

public interface SqlRepository {

    Optional<Sql> find(MethodDefinition identifier);

    void register(Sql sql);

    default void register(Sqls sqls) {
        sqls.list().forEach(this::register);
    }

    Sql get(SqlIdentifier identifier);
}
