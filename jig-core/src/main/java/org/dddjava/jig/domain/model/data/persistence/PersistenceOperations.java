package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.List;

/**
 * 型単位の永続化操作群
 *
 * @param origin                永続化操作群の由来
 * @param persistenceTargets    この型のデフォルト永続化対象。
 * @param persistenceOperations この型に定義されている永続化操作。0件もありえる。
 */
public record PersistenceOperations(
        PersistenceOperationsOrigin origin,
        TypeId typeId,
        PersistenceTargets persistenceTargets,
        Collection<PersistenceOperation> persistenceOperations
) {

    public static PersistenceOperations forMyBatis(TypeId key, List<PersistenceOperation> value) {
        return new PersistenceOperations(PersistenceOperationsOrigin.MYBATIS, key, PersistenceTargets.nothing(), value);
    }

    public static PersistenceOperations forSpringDataJdbc(TypeId typeId, PersistenceTargets persistenceTargets, List<PersistenceOperation> persistenceOperations) {
        return new PersistenceOperations(PersistenceOperationsOrigin.SPRING_DATA_JDBC, typeId, persistenceTargets, persistenceOperations);
    }
}
