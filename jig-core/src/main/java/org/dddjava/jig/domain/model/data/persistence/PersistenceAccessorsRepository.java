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
public record PersistenceAccessorsRepository(Collection<PersistenceAccessors> values) {

    public static PersistenceAccessorsRepository empty() {
        return new PersistenceAccessorsRepository(new ArrayList<>());
    }

    public static PersistenceAccessorsRepository from(Collection<PersistenceAccessors> statements) {
        return new PersistenceAccessorsRepository(new ArrayList<>(statements));
    }

    public Optional<PersistenceAccessors> findByTypeId(TypeId typeId) {
        return values.stream()
                .filter(ops -> ops.typeId().equals(typeId))
                .findAny();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public void register(Collection<PersistenceAccessors> springDataJdbcStatements) {
        // ここで追加するためにvaluesは可変コレクションである必要がある
        values.addAll(springDataJdbcStatements);
    }
}
