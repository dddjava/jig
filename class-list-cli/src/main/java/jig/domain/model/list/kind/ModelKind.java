package jig.domain.model.list.kind;

import jig.domain.model.list.MethodRelationNavigator;
import jig.domain.model.thing.Name;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum ModelKind {
    SERVICE("Service", new MethodRelationNavigator.Concern[]{
            MethodRelationNavigator.Concern.クラス名,
            MethodRelationNavigator.Concern.クラス和名,
            MethodRelationNavigator.Concern.メソッド,
            MethodRelationNavigator.Concern.メソッド戻り値の型,
            MethodRelationNavigator.Concern.使用しているフィールドの型
    }),
    REPOSITORY("Repository", new MethodRelationNavigator.Concern[]{
            MethodRelationNavigator.Concern.クラス名,
            MethodRelationNavigator.Concern.クラス和名,
            MethodRelationNavigator.Concern.メソッド,
            MethodRelationNavigator.Concern.メソッド戻り値の型
    });

    private final String suffix;
    private final MethodRelationNavigator.Concern[] concerns;

    ModelKind(String suffix, MethodRelationNavigator.Concern[] concerns) {
        this.suffix = suffix;
        this.concerns = concerns;
    }

    public List<String> headerLabel() {
        return Arrays.stream(concerns)
                .map(Enum::name)
                .collect(toList());
    }

    public List<String> row(MethodRelationNavigator methodRelationNavigator) {
        return Arrays.stream(concerns)
                .map(concern -> concern.apply(methodRelationNavigator))
                .collect(toList());
    }

    public boolean correct(Name name) {
        return name.value().endsWith(suffix);
    }
}
