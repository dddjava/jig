package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;

import java.util.function.Function;

public enum TypeConcern {
    クラス名(TypeDetail::typeName),
    クラス和名(detail -> detail.japaneseName().value()),
    使用箇所(detail -> "[" + detail.userTypes().asSimpleText() + "]"),
    振る舞い有り(detail -> Boolean.toString(detail.is(Characteristic.ENUM_BEHAVIOUR))),
    パラメーター有り(detail -> Boolean.toString(detail.is(Characteristic.ENUM_PARAMETERIZED))),
    多態(detail -> Boolean.toString(detail.is(Characteristic.ENUM_POLYMORPHISM))),
    定数宣言(detail -> "[" + detail.constants().toNameText() + "]"),
    フィールド(detail -> detail.fieldIdentifiers().toSignatureText());

    private final Function<TypeDetail, String> function;

    TypeConcern(Function<TypeDetail, String> function) {
        this.function = function;
    }

    public String apply(TypeDetail typeDetail) {
        return function.apply(typeDetail);
    }
}
