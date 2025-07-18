package org.dddjava.jig.domain.model.data.rdbaccess;

import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;

import java.util.Collections;
import java.util.List;

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

    public static MyBatisStatements empty() {
        return new MyBatisStatements(SqlReadStatus.未処理);
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
    public MyBatisStatements filterRelationOn(List<MethodCall> methodCalls) {
        List<MyBatisStatement> myBatisStatements = list.stream()
                .filter(myBatisStatement -> myBatisStatement.myBatisStatementId().matches(methodCalls))
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
