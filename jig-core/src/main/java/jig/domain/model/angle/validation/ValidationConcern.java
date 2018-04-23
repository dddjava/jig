package jig.domain.model.angle.validation;

import java.util.function.Function;

public enum ValidationConcern {
    クラス名(ValidationDetail::declaringTypeName),
    クラス和名(detail -> detail.japaneseName().value()),
    フィールドorメソッド(ValidationDetail::annotateSimpleName),
    アノテーション名(detail -> detail.annotationType().asSimpleText()),
    記述(detail -> detail.description().asText());

    private final Function<ValidationDetail, String> function;

    ValidationConcern(Function<ValidationDetail, String> function) {
        this.function = function;
    }

    public String apply(ValidationDetail typeDetail) {
        return function.apply(typeDetail);
    }
}
