package jig.domain.model.list;

import jig.domain.model.relation.Relation;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;

import java.util.function.Function;

import static java.util.stream.Collectors.joining;

public enum ServiceModelConcern implements Converter {
    クラス名(condition ->
            condition.getType().name().value()),
    クラス和名(condition ->
            condition.japaneseName().value()),
    メソッド名(condition ->
            condition.getMethod().name()),
    メソッド戻り値の型(condition ->
            condition.getMethod().returnTypeName().value()),
    メソッド引数型(condition ->
            condition.getMethod().returnTypeName().toString()),
    保持しているフィールドの型(condition ->
            condition.getRegisterRelation().findDependency(condition.getType().name()).list().stream()
                    .map(Relation::to)
                    .map(Thing::name)
                    .map(Name::value)
                    .collect(joining(",")));

    private final Function<ConverterCondition, String> function;

    ServiceModelConcern(Function<ConverterCondition, String> function) {
        this.function = function;
    }

    public String convert(ConverterCondition converterCondition) {
        return function.apply(converterCondition);
    }
}
