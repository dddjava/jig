package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 永続化アクセサ
 *
 * @param typeId                        永続化アクセサをグルーピングする型
 * @param defaultPersistenceTargets     デフォルト永続化対象
 * @param persistenceAccessorOperations この型に定義されている永続化アクセサ処理。0件もありえる。
 * @param technology                    永続化アクセサの実装技術。複数の技術での実装は考慮しない。
 * @param superTypeIds                  この型が実装している型。親インタフェース経由でのMethodCallなどの場合に引き当てるために使用。
 */
public record PersistenceAccessor(
        TypeId typeId,
        Collection<PersistenceTarget> defaultPersistenceTargets,
        Collection<PersistenceAccessorOperation> persistenceAccessorOperations,
        PersistenceAccessorTechnology technology,
        Set<TypeId> superTypeIds
) {

    public static PersistenceAccessor forMyBatis(TypeId key, List<PersistenceAccessorOperation> value) {
        return new PersistenceAccessor(key, List.of(), value, PersistenceAccessorTechnology.MYBATIS, Set.of());
    }

    public static PersistenceAccessor forSpringDataJdbc(TypeId typeId, Collection<PersistenceTarget> defaultPersistenceTargets, List<PersistenceAccessorOperation> persistenceAccessorOperations, Set<TypeId> superTypeIds) {
        return new PersistenceAccessor(typeId, defaultPersistenceTargets, persistenceAccessorOperations, PersistenceAccessorTechnology.SPRING_DATA_JDBC, superTypeIds);
    }

    public Optional<PersistenceAccessorOperation> findPersistenceAccessorById(PersistenceAccessorOperationId persistenceAccessorOperationId) {
        return persistenceAccessorOperations.stream()
                .filter(operation -> operation.id().equals(persistenceAccessorOperationId))
                .findAny();
    }
}
