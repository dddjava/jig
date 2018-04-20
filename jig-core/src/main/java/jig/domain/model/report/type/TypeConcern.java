package jig.domain.model.report.type;

import jig.domain.model.characteristic.Characteristic;

import java.util.function.Function;

public enum TypeConcern {
    クラス名(TypeDetail::typeName),
    クラス和名(detail -> detail.japaneseName().value()),
    使用箇所(detail -> detail.userTypes().asSimpleText()),
    振る舞い有り(detail -> detail.satisfied(Characteristic.ENUM_BEHAVIOUR).toSymbolText()),
    パラメーター有り(detail -> detail.satisfied(Characteristic.ENUM_PARAMETERIZED).toSymbolText()),
    多態(detail -> detail.satisfied(Characteristic.ENUM_POLYMORPHISM).toSymbolText()),
    定数宣言(detail -> detail.constants().toNameText()),
    フィールド(detail -> detail.fieldIdentifiers().toSignatureText());

    private final Function<TypeDetail, String> function;

    TypeConcern(Function<TypeDetail, String> function) {
        this.function = function;
    }

    public String apply(TypeDetail typeDetail) {
        return function.apply(typeDetail);
    }
}
