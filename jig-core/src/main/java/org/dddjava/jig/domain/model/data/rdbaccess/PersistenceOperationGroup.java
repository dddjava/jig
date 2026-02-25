package org.dddjava.jig.domain.model.data.rdbaccess;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

/**
 * DBアクセスグループ
 */
public record PersistenceOperationGroup(
        TypeId typeId,
        Collection<PersistenceOperation> persistenceOperations
) {
}
