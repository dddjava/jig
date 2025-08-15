package org.dddjava.jig.domain.model.data.rdbaccess;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * SQL一覧
 */
public class MyBatisStatements {

    List<MyBatisStatement> list;
    SqlReadStatus sqlReadStatus;

    public MyBatisStatements(SqlReadStatus sqlReadStatus) {
        this(Collections.emptyList(), sqlReadStatus);
    }

    public MyBatisStatements(List<MyBatisStatement> list, SqlReadStatus sqlReadStatus) {
        this.list = list;
        this.sqlReadStatus = sqlReadStatus;
    }

    public Tables tables(SqlType sqlType) {
        return list.stream()
                .filter(myBatisStatement -> myBatisStatement.sqlType() == sqlType)
                .map(MyBatisStatement::tables)
                .reduce(Tables::merge)
                .orElse(Tables.nothing());
    }

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
        return new MyBatisStatements(myBatisStatements, sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && list.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
