package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * SQL一覧
 */
public record MyBatisStatements(List<MyBatisStatement> list) {

    public static MyBatisStatements empty() {
        return new MyBatisStatements(Collections.emptyList());
    }

    private Tables tables(SqlType sqlType) {
        return list.stream()
                .filter(myBatisStatement -> myBatisStatement.sqlType() == sqlType)
                .map(MyBatisStatement::tables)
                .reduce(Tables::merge)
                .orElse(Tables.nothing());
    }

    // テストでしか使用していないので削除したい
    @Deprecated
    public List<MyBatisStatement> list() {
        return list;
    }

    /**
     * 引数のメソッドに関連するステートメントに絞り込む
     */
    public MyBatisStatements filterRelationOn(Predicate<MyBatisStatement> myBatisStatementPredicate) {
        List<MyBatisStatement> myBatisStatements = list.stream()
                .filter(myBatisStatementPredicate)
                .toList();
        return new MyBatisStatements(myBatisStatements);
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
