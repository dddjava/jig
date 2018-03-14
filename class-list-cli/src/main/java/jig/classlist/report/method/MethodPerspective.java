package jig.classlist.report.method;

import jig.domain.model.tag.Tag;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

public enum MethodPerspective {
    SERVICE(new MethodConcern[]{
            MethodConcern.クラス名,
            MethodConcern.クラス和名,
            MethodConcern.メソッド,
            MethodConcern.メソッド戻り値の型,
            MethodConcern.使用しているフィールドの型
    }),
    REPOSITORY(new MethodConcern[]{
            MethodConcern.クラス名,
            MethodConcern.クラス和名,
            MethodConcern.メソッド,
            MethodConcern.メソッド戻り値の型
    });

    private final MethodConcern[] concerns;

    MethodPerspective(MethodConcern[] concerns) {
        this.concerns = concerns;
    }

    public List<String> headerLabel() {
        return Arrays.stream(concerns)
                .map(Enum::name)
                .collect(toList());
    }

    public List<String> row(MethodDetail methodDetail) {
        return Arrays.stream(concerns)
                .map(concern -> concern.apply(methodDetail))
                .collect(toList());
    }

    public static MethodPerspective from(Tag tag) {
        if (tag == Tag.SERVICE) return SERVICE;
        if (tag == Tag.REPOSITORY) return REPOSITORY;
        throw new IllegalArgumentException(tag.toString());
    }
}
