package jig.domain.model.usage;

import java.util.Arrays;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.joining;

public enum ModelConcern {
    クラス名((modelType, m) ->
            modelType.name().value()),
    クラス和名((modelType, m) ->
            modelType.japaneseName().value()),
    メソッド名((t, method) ->
            method.name()),
    メソッド戻り値の型((t, method) ->
            method.returnType().getSimpleName()),
    メソッド引数型((t, method) ->
            Arrays.stream(method.parameters())
                    .map(Class::getSimpleName)
                    .collect(joining(","))),
    保持しているフィールドの型((modelType, m) ->
            modelType.dependents().list().stream()
                    .map(Class::getSimpleName)
                    .collect(joining(",")));

    private final BiFunction<ModelType, ModelMethod, String> function;

    ModelConcern(BiFunction<ModelType, ModelMethod, String> function) {
        this.function = function;
    }

    public String apply(ModelType type, ModelMethod method) {
        return function.apply(type, method);
    }
}
