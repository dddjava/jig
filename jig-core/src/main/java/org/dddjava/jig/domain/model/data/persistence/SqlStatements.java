package org.dddjava.jig.domain.model.data.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * SQL一覧
 */
public record SqlStatements(Collection<PersistenceOperations> values) {

    public static SqlStatements empty() {
        return new SqlStatements(Collections.emptyList());
    }

    public static SqlStatements from(Collection<PersistenceOperations> statements) {
        return new SqlStatements(statements);
    }

    private PersistenceTargets tables(SqlType sqlType) {
        return values.stream()
                .flatMap(ops -> ops.persistenceOperations().stream())
                .filter(sqlStatement -> sqlStatement.sqlType() == sqlType)
                .map(PersistenceOperation::persistenceTargets)
                .reduce(PersistenceTargets::merge)
                .orElse(PersistenceTargets.nothing());
    }

    public Optional<PersistenceOperation> findById(PersistenceOperationId persistenceOperationId) {
        return values.stream()
                .flatMap(ops -> ops.persistenceOperations().stream())
                .filter(sqlStatement -> sqlStatement.persistenceOperationId().equals(persistenceOperationId))
                .findFirst();
    }

    /**
     * 引数のメソッドに関連するステートメントに絞り込む
     */
    public SqlStatements filterRelationOn(Predicate<PersistenceOperation> sqlStatementPredicate) {
        Collection<PersistenceOperations> filteredOperations = values.stream()
                .map(ops -> new PersistenceOperations(
                        ops.typeId(),
                        ops.persistenceOperations().stream()
                                .filter(sqlStatementPredicate)
                                .toList()
                ))
                .filter(ops -> !ops.persistenceOperations().isEmpty())
                .toList();
        return new SqlStatements(filteredOperations);
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
