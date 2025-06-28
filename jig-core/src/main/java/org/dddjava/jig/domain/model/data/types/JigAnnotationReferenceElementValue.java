package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public sealed interface JigAnnotationReferenceElementValue permits
        JigAnnotationReferenceElementNormalValue,
        JigAnnotationReferenceElementClassValue,
        JigAnnotationReferenceElementEnumValue,
        JigAnnotationInstanceElementAnnotationValue,
        JigAnnotationReferenceElementArray {
    String valueText();
}

/**
 * アノテーションの要素値が配列
 *
 * <code>@Hoge(value = {...})</code>
 */
record JigAnnotationReferenceElementArray(List<JigAnnotationReferenceElementValue> values)
        implements JigAnnotationReferenceElementValue {
    @Override
    public String valueText() {
        // Java言語では配列と定義されていても、1件の場合は `{}` を書いても書かなくてもよい。
        // コンパイル後にはどちらで書かれていたか白別できないが、一般的に1件の場合には記述しないのでそちらの表記に近づける。
        if (values.size() == 1) return values.get(0).valueText();

        return values.stream().map(JigAnnotationReferenceElementValue::valueText).collect(joining(", ", "{", "}"));
    }
}

/**
 * アノテーションの要素値が通常の値（配列、クラスリテラル、enumの列挙でないもの）
 *
 * <code>@Hoge(value = "hoge")</code>
 */
record JigAnnotationReferenceElementNormalValue(Object value)
        implements JigAnnotationReferenceElementValue {
    @Override
    public String valueText() {
        return Objects.toString(value);
    }
}

/**
 * アノテーションの要素値がクラスリテラル
 *
 * <code>@Hoge(value = Hoge.class)</code>
 */
record JigAnnotationReferenceElementClassValue(TypeIdentifier value)
        implements JigAnnotationReferenceElementValue {
    @Override
    public String valueText() {
        return value.asSimpleName();
    }
}

/**
 * アノテーションの要素値がenumの定数
 *
 * <code>@Hoge(value = MyEnum.HOGE)</code>
 *
 * @param typeIdentifier enumの型
 * @param constantName   列挙値のname
 */
record JigAnnotationReferenceElementEnumValue(TypeIdentifier typeIdentifier, String constantName)
        implements JigAnnotationReferenceElementValue {
    @Override
    public String valueText() {
        return typeIdentifier.asSimpleName() + "." + constantName;
    }
}

/**
 * アノテーションの要素値がアノテーション
 *
 * <code>@Hoge(value = @Fuga)</code>
 */
record JigAnnotationInstanceElementAnnotationValue(TypeIdentifier typeIdentifier,
                                                   Collection<JigAnnotationElementValuePair> elements)
        implements JigAnnotationReferenceElementValue {
    @Override
    public String valueText() {
        // どこまでも深くなるので必要になるまで簡略出力としておく
        return "@" + typeIdentifier.asSimpleName() + "(...)";
    }
}
