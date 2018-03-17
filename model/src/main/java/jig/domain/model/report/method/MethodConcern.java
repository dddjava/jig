package jig.domain.model.report.method;

import java.util.function.Function;

public enum MethodConcern {
    クラス名(detail -> detail.name().value()),
    クラス和名(detail -> detail.japaneseName().value()),
    メソッド(detail -> detail.methodName().asSimpleText()),
    メソッド戻り値の型(detail -> detail.returnTypeName().value()),
    使用しているフィールドの型(detail -> detail.instructFields().asCompressText()),
    データソースメソッド(detail -> detail.datasourceMethod().value()),
    使用しているMapperメソッド(detail -> detail.instructMapperMethodNames().asCompressText()),
    データベース操作(detail -> detail.sqls().crud());

    private final Function<MethodDetail, String> function;

    MethodConcern(Function<MethodDetail, String> function) {
        this.function = function;
    }

    public String apply(MethodDetail methodDetail) {
        return function.apply(methodDetail);
    }
}
