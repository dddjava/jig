package org.dddjava.jig.domain.model.datasource;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

import java.util.Optional;

public interface SqlRepository {

    Optional<Sql> find(MethodDeclaration identifier);

    void register(Sql sql);

    default void register(Sqls sqls) {
        sqls.list().forEach(this::register);
    }
}
