package jig.domain.model.list;

import java.util.function.Function;

public enum RepositoryModelConcern implements Converter {
    クラス名(condition ->
            condition.getType().name().value()),
    クラス和名(condition ->
            condition.japaneseName().value()),
    メソッド名(condition ->
            condition.getMethod().name()),
    メソッド戻り値の型(condition ->
            condition.getMethod().returnTypeName().value()),
    メソッド引数型(condition ->
            condition.getMethod().parameters().toString());

    private final Function<ConverterCondition, String> function;

    RepositoryModelConcern(Function<ConverterCondition, String> function) {
        this.function = function;
    }

    public String convert(ConverterCondition converterCondition) {
        return function.apply(converterCondition);
    }
}
