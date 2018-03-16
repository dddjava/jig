package jig.domain.model.report.method;

import jig.domain.model.report.ReportRow;
import jig.domain.model.tag.Tag;

import java.util.Arrays;

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

    public ReportRow headerLabel() {
        return Arrays.stream(concerns)
                .map(Enum::name)
                .collect(ReportRow.collector());
    }

    public ReportRow row(MethodDetail methodDetail) {
        return Arrays.stream(concerns)
                .map(concern -> concern.apply(methodDetail))
                .collect(ReportRow.collector());
    }

    public static MethodPerspective from(Tag tag) {
        if (tag == Tag.SERVICE) return SERVICE;
        if (tag == Tag.REPOSITORY) return REPOSITORY;
        throw new IllegalArgumentException(tag.toString());
    }
}
