package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

public sealed interface JigAnnotationElementValue permits
        JigAnnotationElementNormalValue,
        JigAnnotationElementEnumValue,
        JigAnnotationElementClassValue,
        JigAnnotationElementArray,
        JigAnnotationInstanceElementAnnotationValue {
    String valueText();
}

/**
 * アノテーションの要素値が通常の値（配列、クラスリテラル、enumの列挙でないもの）
 *
 * <code>@Hoge(value = "hoge")</code>
 */
record JigAnnotationElementNormalValue(Object value)
        implements JigAnnotationElementValue {
    @Override
    public String valueText() {
        return Objects.toString(value);
    }
}

/**
 * アノテーションの要素値がenumの定数
 *
 * <code>@Hoge(value = MyEnum.HOGE)</code>
 *
 * @param typeId enumの型
 * @param constantName   列挙値のname
 */
record JigAnnotationElementEnumValue(TypeId typeId, String constantName)
        implements JigAnnotationElementValue {
    @Override
    public String valueText() {
        return typeId.asSimpleName() + "." + constantName;
    }
}

/**
 * アノテーションの要素値がクラスリテラル
 *
 * <code>@Hoge(value = Hoge.class)</code>
 */
record JigAnnotationElementClassValue(TypeId value)
        implements JigAnnotationElementValue {
    @Override
    public String valueText() {
        return value.asSimpleName();
    }
}

/**
 * アノテーションの要素値が配列
 *
 * <code>@Hoge(value = {...})</code>
 */
record JigAnnotationElementArray(List<JigAnnotationElementValue> values)
        implements JigAnnotationElementValue {
    @Override
    public String valueText() {
        // Java言語では配列と定義されていても、1件の場合は `{}` を書いても書かなくてもよい。
        // コンパイル後にはどちらで書かれていたか白別できないが、一般的に1件の場合には記述しないのでそちらの表記に近づける。
        if (values.size() == 1) return values.get(0).valueText();

        return values.stream().map(JigAnnotationElementValue::valueText).collect(joining(", ", "{", "}"));
    }
}

/**
 * アノテーションの要素値がアノテーション
 *
 * <code>@Hoge(value = @Fuga)</code>
 */
record JigAnnotationInstanceElementAnnotationValue(TypeId typeId,
                                                   Collection<JigAnnotationElementValuePair> elements)
        implements JigAnnotationElementValue {
    @Override
    public String valueText() {
        // どこまでも深くなるので必要になるまで簡略出力としておく
        return "@" + typeId.asSimpleName() + "(...)";
    }
}
