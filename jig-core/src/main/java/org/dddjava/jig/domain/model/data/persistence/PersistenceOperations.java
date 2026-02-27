package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

/**
 * 型単位の永続化操作群
 *
 * @param persistenceOperations この型に定義されている永続化操作。0件もありえる。
 */
public record PersistenceOperations(
        TypeId typeId,
        Collection<PersistenceOperation> persistenceOperations
) {
}
