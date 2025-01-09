package org.dddjava.jig.domain.model.data.classes.rdbaccess;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public MyBatisStatements filterRelationOn(MethodDeclarations methodDeclarations) {
        List<MyBatisStatement> myBatisStatements = list.stream()
                .filter(myBatisStatement -> myBatisStatement.identifier().matches(methodDeclarations))
                .collect(Collectors.toList());
        return new MyBatisStatements(myBatisStatements, sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && list.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
