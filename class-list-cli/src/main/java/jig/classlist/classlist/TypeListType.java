package jig.classlist.classlist;

import jig.domain.model.tag.Tag;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum TypeListType {
    IDENTIFIER(Tag.IDENTIFIER,
            TypeListNavigator.Concern.クラス名,
            TypeListNavigator.Concern.クラス和名,
            TypeListNavigator.Concern.使用箇所),
    NUMBER(Tag.NUMBER,
            TypeListNavigator.Concern.クラス名,
            TypeListNavigator.Concern.クラス和名,
            TypeListNavigator.Concern.使用箇所),
    ENUM(Tag.ENUM,
            TypeListNavigator.Concern.クラス名,
            TypeListNavigator.Concern.クラス和名,
            TypeListNavigator.Concern.使用箇所,
            TypeListNavigator.Concern.パラメーター有り,
            TypeListNavigator.Concern.振る舞い有り,
            TypeListNavigator.Concern.多態),
    TERM(Tag.TERM,
            TypeListNavigator.Concern.クラス名,
            TypeListNavigator.Concern.クラス和名,
            TypeListNavigator.Concern.使用箇所),
    DATE(Tag.DATE,
            TypeListNavigator.Concern.クラス名,
            TypeListNavigator.Concern.クラス和名,
            TypeListNavigator.Concern.使用箇所),
    COLLECTION(Tag.COLLECTION,
            TypeListNavigator.Concern.クラス名,
            TypeListNavigator.Concern.クラス和名,
            TypeListNavigator.Concern.使用箇所);

    private final TypeListNavigator.Concern[] concerns;

    TypeListType(Tag tag, TypeListNavigator.Concern... concerns) {
        this.concerns = concerns;
    }

    public List<String> headerLabel() {
        return Arrays.stream(concerns)
                .map(Enum::name)
                .collect(toList());
    }

    public List<String> row(TypeListNavigator navigator) {
        return Arrays.stream(concerns)
                .map(concern -> concern.apply(navigator))
                .collect(toList());
    }
}
