package jig.domain.model.list;

import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Thing;
import jig.domain.model.usage.ModelMethod;
import jig.domain.model.usage.ModelType;

import java.util.Arrays;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

public enum ServiceModelConcern implements Converter {
    クラス名(condition ->
            condition.modelType.name().value()),
    クラス和名(condition ->
            condition.modelType.japaneseName().value()),
    メソッド名(condition ->
            condition.modelMethod.name()),
    メソッド戻り値の型(condition ->
            condition.modelMethod.returnType().getSimpleName()),
    メソッド引数型(condition ->
            Arrays.stream(condition.modelMethod.parameters())
                    .map(Class::getSimpleName)
                    .collect(joining(","))),
    保持しているフィールドの型(condition ->
            condition.relationRepository.findDependency(condition.modelType.name()).list().stream()
                    .map(Relation::to)
                    .map(Thing::name)
                    .map(Name::value)
                    .collect(joining(",")));

    private final Function<Condition, String> function;

    ServiceModelConcern(Function<Condition, String> function) {
        this.function = function;
    }

    public String convert(ModelType type, ModelMethod method, RelationRepository registerRelation) {
        return function.apply(new Condition(type, method, registerRelation));
    }

    static class Condition {
        ModelType modelType;
        ModelMethod modelMethod;
        RelationRepository relationRepository;

        public Condition(ModelType modelType, ModelMethod modelMethod, RelationRepository relationRepository) {
            this.modelType = modelType;
            this.modelMethod = modelMethod;
            this.relationRepository = relationRepository;
        }
    }
}
