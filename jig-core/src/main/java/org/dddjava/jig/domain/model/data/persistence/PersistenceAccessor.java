package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 永続化アクセサ
 *
 * @param typeId                        永続化アクセサをグルーピングする型
 * @param defaultPersistenceTargets     デフォルト永続化対象
 * @param persistenceAccessorOperations この型に定義されている永続化アクセサ処理。0件もありえる。
 * @param technology                    永続化アクセサの実装技術。複数の技術での実装は考慮しない。
 */
public record PersistenceAccessor(
        TypeId typeId,
        PersistenceTargets defaultPersistenceTargets,
        Collection<PersistenceAccessorOperation> persistenceAccessorOperations,
        PersistenceAccessorTechnology technology
) {

    public static PersistenceAccessor forMyBatis(TypeId key, List<PersistenceAccessorOperation> value) {
        return new PersistenceAccessor(key, PersistenceTargets.nothing(), value, PersistenceAccessorTechnology.MYBATIS);
    }

    public static PersistenceAccessor forSpringDataJdbc(TypeId typeId, PersistenceTargets defaultPersistenceTargets, List<PersistenceAccessorOperation> persistenceAccessorOperations) {
        return new PersistenceAccessor(typeId, defaultPersistenceTargets, persistenceAccessorOperations, PersistenceAccessorTechnology.SPRING_DATA_JDBC);
    }

    public Optional<PersistenceAccessorOperation> findPersistenceAccessorById(PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return persistenceAccessorOperations.stream()
                .filter(operation -> operation.persistenceAccessorOperationId().equals(persistenceAccessorOperationId))
                .findAny();
    }
}
