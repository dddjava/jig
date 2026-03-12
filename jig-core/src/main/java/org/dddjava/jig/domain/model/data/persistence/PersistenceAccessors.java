package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 永続化アクセサ群
 *
 * @param typeId                    永続化アクセサをグルーピングする型
 * @param defaultPersistenceTargets デフォルト永続化対象
 * @param persistenceAccessors      この型に定義されている永続化アクセサ。0件もありえる。
 */
public record PersistenceAccessors(
        PersistenceAccessorTechnology technology,
        TypeId typeId,
        PersistenceTargets defaultPersistenceTargets,
        Collection<PersistenceAccessor> persistenceAccessors
) {

    public static PersistenceAccessors forMyBatis(TypeId key, List<PersistenceAccessor> value) {
        return new PersistenceAccessors(PersistenceAccessorTechnology.MYBATIS, key, PersistenceTargets.nothing(), value);
    }

    public static PersistenceAccessors forSpringDataJdbc(TypeId typeId, PersistenceTargets defaultPersistenceTargets, List<PersistenceAccessor> persistenceAccessors) {
        return new PersistenceAccessors(PersistenceAccessorTechnology.SPRING_DATA_JDBC, typeId, defaultPersistenceTargets, persistenceAccessors);
    }

    public Optional<PersistenceAccessor> findPersistenceAccessorById(PersistenceAccessorId persistenceAccessorId) {
        return persistenceAccessors.stream()
                .filter(operation -> operation.persistenceAccessorId().equals(persistenceAccessorId))
                .findAny();
    }
}
