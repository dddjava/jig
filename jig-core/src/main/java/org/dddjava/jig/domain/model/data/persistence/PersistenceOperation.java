package org.dddjava.jig.domain.model.data.persistence;

/**
 * 永続化操作
 *
 * 永続化対象と操作種類のペア
 */
public record PersistenceOperation(PersistenceTarget persistenceTarget, PersistenceOperationType operationType) {

    public static PersistenceOperation from(PersistenceTarget persistenceTarget, PersistenceOperationType operationType) {
        return new PersistenceOperation(persistenceTarget, operationType);
    }
}
