package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.report.ReportRow;

import java.util.Arrays;

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

    public ReportRow headerLabel() {
        return Arrays.stream(concerns)
                .map(Enum::name)
                .collect(ReportRow.collector());
    }

    public ReportRow row(TypeDetail detail) {
        return Arrays.stream(concerns)
                .map(concern -> concern.apply(detail))
                .collect(ReportRow.collector());
    }

    public static TypePerspective from(Characteristic characteristic) {
        if (characteristic.matches(Characteristic.ENUM)) return ENUM;
        return valueOf(characteristic.name());
    }

    public Characteristic characteristic() {
        return Characteristic.valueOf(name());
    }
}
