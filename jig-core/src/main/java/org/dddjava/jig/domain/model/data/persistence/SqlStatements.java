package org.dddjava.jig.domain.model.data.persistence;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * SQL一覧
 */
public record SqlStatements(List<PersistenceOperation> list) {

    public static SqlStatements empty() {
        return new SqlStatements(Collections.emptyList());
    }

    public static SqlStatements from(Collection<PersistenceOperations> statements) {
        // SqlStatementsが直接SqlStatementGroupのコレクションを保持するようにするまでのつなぎ
        return new SqlStatements(statements.stream()
                .flatMap(persistenceOperations -> persistenceOperations.persistenceOperations().stream())
                .toList());
    }

    private PersistenceTargets tables(SqlType sqlType) {
        return list.stream()
                .filter(sqlStatement -> sqlStatement.sqlType() == sqlType)
                .map(PersistenceOperation::persistenceTargets)
                .reduce(PersistenceTargets::merge)
                .orElse(PersistenceTargets.nothing());
    }

    public Optional<PersistenceOperation> findById(PersistenceOperationId persistenceOperationId) {
        return list.stream()
                .filter(sqlStatement -> sqlStatement.persistenceOperationId().equals(persistenceOperationId))
                .findFirst();
    }

    /**
     * 引数のメソッドに関連するステートメントに絞り込む
     */
    public SqlStatements filterRelationOn(Predicate<PersistenceOperation> sqlStatementPredicate) {
        List<PersistenceOperation> persistenceOperations = list.stream()
                .filter(sqlStatementPredicate)
                .toList();
        return new SqlStatements(persistenceOperations);
    }

    public boolean isEmpty() {
        return list.isEmpty();
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
