package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * 永続化操作リポジトリ
 *
 * @param values 永続化操作群を保持する可変コレクション
 */
public record PersistenceOperationsRepository(Collection<PersistenceOperations> values) {

    public static PersistenceOperationsRepository empty() {
        return new PersistenceOperationsRepository(new ArrayList<>());
    }

    public static PersistenceOperationsRepository from(Collection<PersistenceOperations> statements) {
        return new PersistenceOperationsRepository(new ArrayList<>(statements));
    }

    public Optional<PersistenceOperations> findByTypeId(TypeId typeId) {
        return values.stream()
                .filter(ops -> ops.typeId().equals(typeId))
                .findAny();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void register(Collection<PersistenceOperations> springDataJdbcStatements) {
        // ここで追加するためにvaluesは可変コレクションである必要がある
        values.addAll(springDataJdbcStatements);
    }
}
