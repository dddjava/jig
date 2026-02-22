package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * SQL一覧
 */
public record SqlStatements(List<SqlStatement> list) {

    public static SqlStatements empty() {
        return new SqlStatements(Collections.emptyList());
    }

    private Tables tables(SqlType sqlType) {
        return list.stream()
                .filter(sqlStatement -> sqlStatement.sqlType() == sqlType)
                .map(SqlStatement::tables)
                .reduce(Tables::merge)
                .orElse(Tables.nothing());
    }

    public Optional<SqlStatement> findById(SqlStatementId sqlStatementId) {
        return list.stream()
                .filter(sqlStatement -> sqlStatement.sqlStatementId().equals(sqlStatementId))
                .findFirst();
    }

    /**
     * 引数のメソッドに関連するステートメントに絞り込む
     */
    public SqlStatements filterRelationOn(Predicate<SqlStatement> sqlStatementPredicate) {
        List<SqlStatement> sqlStatements = list.stream()
                .filter(sqlStatementPredicate)
                .toList();
        return new SqlStatements(sqlStatements);
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
