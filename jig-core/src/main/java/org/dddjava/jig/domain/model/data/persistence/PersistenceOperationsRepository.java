package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * 永続化操作リポジトリ
 */
public record PersistenceOperationsRepository(Collection<PersistenceOperations> values) {

    public static PersistenceOperationsRepository empty() {
        return new PersistenceOperationsRepository(Collections.emptyList());
    }

    public static PersistenceOperationsRepository from(Collection<PersistenceOperations> statements) {
        return new PersistenceOperationsRepository(statements);
    }

    public Optional<PersistenceOperations> findByTypeId(TypeId typeId) {
        return values.stream()
                .filter(ops -> ops.typeId().equals(typeId))
                .findAny();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
