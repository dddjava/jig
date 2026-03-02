package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.persistence.PersistenceOperation;
import org.dddjava.jig.domain.model.data.persistence.PersistenceOperationId;
import org.dddjava.jig.domain.model.information.members.JigMethod;

import java.util.Collection;

/**
 * 出力アダプタの実装
 */
public record OutputAdapterExecution(JigMethod jigMethod, Collection<PersistenceOperation> persistenceOperations) {

    public boolean uses(PersistenceOperationId persistenceOperationId) {
        return persistenceOperations.stream()
                .anyMatch(persistenceOperation -> persistenceOperation.persistenceOperationId().equals(persistenceOperationId));
    }
}
