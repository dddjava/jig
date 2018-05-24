package org.dddjava.jig.domain.model.implementation.datasource;

public interface SqlRepository {

    void register(Sql sql);

    default void register(Sqls sqls) {
        sqls.list().forEach(this::register);
    }

    Sqls all();
}
