package jig.classlist.report.type;

import jig.domain.model.tag.Tag;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum TypePerspective {
    IDENTIFIER(
            TypeConcern.クラス名,
            TypeConcern.クラス和名,
            TypeConcern.使用箇所),
    NUMBER(
            TypeConcern.クラス名,
            TypeConcern.クラス和名,
            TypeConcern.使用箇所),
    ENUM(
            TypeConcern.クラス名,
            TypeConcern.クラス和名,
            TypeConcern.使用箇所,
            TypeConcern.パラメーター有り,
            TypeConcern.振る舞い有り,
            TypeConcern.多態),
    TERM(
            TypeConcern.クラス名,
            TypeConcern.クラス和名,
            TypeConcern.使用箇所),
    DATE(
            TypeConcern.クラス名,
            TypeConcern.クラス和名,
            TypeConcern.使用箇所),
    COLLECTION(
            TypeConcern.クラス名,
            TypeConcern.クラス和名,
            TypeConcern.使用箇所);

    private final TypeConcern[] concerns;

    TypePerspective(TypeConcern... concerns) {
        this.concerns = concerns;
    }

    public List<String> headerLabel() {
        return Arrays.stream(concerns)
                .map(Enum::name)
                .collect(toList());
    }

    public List<String> row(TypeDetail detail) {
        return Arrays.stream(concerns)
                .map(concern -> concern.apply(detail))
                .collect(toList());
    }

    public static TypePerspective from(Tag tag) {
        if (tag.matches(Tag.ENUM)) return ENUM;
        return valueOf(tag.name());
    }
}
