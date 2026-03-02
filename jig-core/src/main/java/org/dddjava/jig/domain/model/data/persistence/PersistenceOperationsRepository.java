package org.dddjava.jig.domain.model.data.persistence;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 永続化操作リポジトリ
 */
public record PersistenceOperationsRepository(Collection<PersistenceOperations> values) {

    public static PersistenceOperationsRepository empty() {
        return new PersistenceOperationsRepository(Collections.emptyList());
    }

    public static PersistenceOperationsRepository from(Collection<PersistenceOperations> statements) {
        return new PersistenceOperationsRepository(statements);
    }

    private PersistenceTargets tables(SqlType sqlType) {
        return values.stream()
                .flatMap(ops -> ops.persistenceOperations().stream())
                .filter(sqlStatement -> sqlStatement.sqlType() == sqlType)
                .map(PersistenceOperation::persistenceTargets)
                .reduce(PersistenceTargets::merge)
                .orElse(PersistenceTargets.nothing());
    }

    public Optional<PersistenceOperations> findByTypeId(TypeId typeId) {
        return values.stream()
                .filter(ops -> ops.typeId().equals(typeId))
                .findAny();
    }

    /**
     * 引数のメソッドに関連するステートメントに絞り込む
     */
    public PersistenceOperationsRepository filterRelationOn(Predicate<PersistenceOperation> sqlStatementPredicate) {
        Collection<PersistenceOperations> filteredOperations = values.stream()
                .map(ops -> new PersistenceOperations(
                        ops.origin(),
                        ops.typeId(),
                        ops.persistenceOperations().stream()
                                .filter(sqlStatementPredicate)
                                .toList()
                ))
                .filter(ops -> !ops.persistenceOperations().isEmpty())
                .toList();
        return new PersistenceOperationsRepository(filteredOperations);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public CrudTables crudTables() {
        return new CrudTables(
                tables(SqlType.INSERT),
                tables(SqlType.SELECT),
                tables(SqlType.UPDATE),
                tables(SqlType.DELETE)
        );
    }
}
