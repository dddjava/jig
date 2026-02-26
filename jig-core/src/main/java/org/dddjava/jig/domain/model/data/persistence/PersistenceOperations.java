package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

/**
 * 型単位の永続化操作群
 */
public record PersistenceOperations(
        TypeId typeId,
        Collection<PersistenceOperation> persistenceOperations
) {
}
