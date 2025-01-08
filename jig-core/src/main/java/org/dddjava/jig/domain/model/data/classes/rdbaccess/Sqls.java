package org.dddjava.jig.domain.model.data.classes.rdbaccess;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL一覧
 */
public class Sqls {

    List<Sql> list;
    SqlReadStatus sqlReadStatus;

    public Sqls(SqlReadStatus sqlReadStatus) {
        this(Collections.emptyList(), sqlReadStatus);
    }

    public Sqls(List<Sql> list, SqlReadStatus sqlReadStatus) {
        this.list = list;
        this.sqlReadStatus = sqlReadStatus;
    }

    public static Sqls empty() {
        return new Sqls(SqlReadStatus.未処理);
    }

    public Tables tables(SqlType sqlType) {
        return list.stream()
                .filter(sql -> sql.sqlType() == sqlType)
                .map(Sql::tables)
                .reduce(Tables::merge)
                .orElse(Tables.nothing());
    }

    public List<Sql> list() {
        return list;
    }

    public Sqls filterRelationOn(MethodDeclarations methodDeclarations) {
        List<Sql> sqls = list.stream()
                .filter(sql -> sql.identifier().matches(methodDeclarations))
                .collect(Collectors.toList());
        return new Sqls(sqls, sqlReadStatus);
    }

    public SqlReadStatus status() {
        if (sqlReadStatus == SqlReadStatus.成功 && list.isEmpty()) {
            return SqlReadStatus.SQLなし;
        }
        return sqlReadStatus;
    }
}
