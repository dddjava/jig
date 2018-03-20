package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;

import java.util.function.Function;

public enum TypeConcern {
    クラス名(detail -> detail.name().value()),
    クラス和名(detail -> detail.japaneseName().value()),
    使用箇所(detail -> detail.usage().asCompressText()),
    振る舞い有り(detail -> Boolean.toString(detail.is(Characteristic.ENUM_BEHAVIOUR))),
    パラメーター有り(detail -> Boolean.toString(detail.is(Characteristic.ENUM_PARAMETERIZED))),
    多態(detail -> Boolean.toString(detail.is(Characteristic.ENUM_POLYMORPHISM))),;

    private final Function<TypeDetail, String> function;

    TypeConcern(Function<TypeDetail, String> function) {
        this.function = function;
    }

    public String apply(TypeDetail typeDetail) {
        return function.apply(typeDetail);
    }
}
