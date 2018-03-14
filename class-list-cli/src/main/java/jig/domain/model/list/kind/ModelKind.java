package jig.domain.model.list.kind;

import jig.domain.model.list.MethodRelationNavigator;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum ModelKind {
    SERVICE(new MethodRelationNavigator.Concern[]{
            MethodRelationNavigator.Concern.クラス名,
            MethodRelationNavigator.Concern.クラス和名,
            MethodRelationNavigator.Concern.メソッド,
            MethodRelationNavigator.Concern.メソッド戻り値の型,
            MethodRelationNavigator.Concern.使用しているフィールドの型
    }),
    REPOSITORY(new MethodRelationNavigator.Concern[]{
            MethodRelationNavigator.Concern.クラス名,
            MethodRelationNavigator.Concern.クラス和名,
            MethodRelationNavigator.Concern.メソッド,
            MethodRelationNavigator.Concern.メソッド戻り値の型
    });

    private final MethodRelationNavigator.Concern[] concerns;

    ModelKind(MethodRelationNavigator.Concern[] concerns) {
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
}
