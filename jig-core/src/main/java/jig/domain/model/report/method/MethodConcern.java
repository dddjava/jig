package jig.domain.model.report.method;

import jig.domain.model.datasource.SqlType;

import java.util.function.Function;

public enum MethodConcern {
    クラス名(MethodDetail::typeName),
    クラス和名(detail -> detail.japaneseName().value()),
    メソッド(detail -> detail.method().asSimpleText()),
    メソッド戻り値の型(detail -> detail.returnType().asSimpleText()),
    使用しているフィールドの型(detail -> "[" + detail.usingFieldTypes().asSimpleText() + "]"),

    DB_C(detail -> detail.sqls().tables(SqlType.INSERT).asText()),
    DB_R(detail -> detail.sqls().tables(SqlType.SELECT).asText()),
    DB_U(detail -> detail.sqls().tables(SqlType.UPDATE).asText()),
    DB_D(detail -> detail.sqls().tables(SqlType.DELETE).asText());

    private final Function<MethodDetail, String> function;

    MethodConcern(Function<MethodDetail, String> function) {
        this.function = function;
    }

    public String apply(MethodDetail methodDetail) {
        return function.apply(methodDetail);
    }
}
