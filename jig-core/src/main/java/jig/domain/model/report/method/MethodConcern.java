package jig.domain.model.report.method;

import jig.domain.model.datasource.SqlType;

import java.util.function.Function;

public enum MethodConcern {
    クラス名(detail -> detail.typeIdentifier().value()),
    クラス和名(detail -> detail.japaneseName().value()),
    メソッド(detail -> detail.methodIdentifier().asSimpleText()),
    メソッド戻り値の型(detail -> detail.returnTypeIdentifier().asSimpleText()),
    使用しているフィールドの型(detail -> detail.instructFields().asSimpleText()),

    DB_C(detail -> detail.sqls().tables(SqlType.INSERT)),
    DB_R(detail -> detail.sqls().tables(SqlType.SELECT)),
    DB_U(detail -> detail.sqls().tables(SqlType.UPDATE)),
    DB_D(detail -> detail.sqls().tables(SqlType.DELETE));

    private final Function<MethodDetail, String> function;

    MethodConcern(Function<MethodDetail, String> function) {
        this.function = function;
    }

    public String apply(MethodDetail methodDetail) {
        return function.apply(methodDetail);
    }
}
