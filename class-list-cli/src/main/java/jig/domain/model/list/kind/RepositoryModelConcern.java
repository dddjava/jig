package jig.domain.model.list.kind;

import jig.domain.model.list.ConverterCondition;

import java.util.function.Function;

public enum RepositoryModelConcern implements Converter {
    クラス名(condition -> condition.className().value()),
    クラス和名(condition -> condition.japaneseName().value()),
    メソッド(condition -> condition.methodName().shortText()),
    メソッド戻り値の型(condition -> condition.returnTypeName().value());

    private final Function<ConverterCondition, String> function;

    RepositoryModelConcern(Function<ConverterCondition, String> function) {
        this.function = function;
    }

    public String convert(ConverterCondition converterCondition) {
        return function.apply(converterCondition);
    }
}
