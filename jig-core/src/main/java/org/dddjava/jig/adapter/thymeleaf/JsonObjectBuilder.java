package org.dddjava.jig.adapter.thymeleaf;

import java.util.ArrayList;
import java.util.List;

/**
 * JSONオブジェクトを組み立てるビルダー。
 * {@link Json#object(String, Object)} で開始し、{@link #and} でプロパティを追加する。
 */
public final class JsonObjectBuilder {

    private final List<String> pairs = new ArrayList<>();

    JsonObjectBuilder() {
    }

    /**
     * プロパティを追加する。文字列は自動エスケープされる。
     *
     * @param key   プロパティ名
     * @param value プロパティ値（String, Number, Boolean, JsonRaw, List&lt;String&gt; など）
     * @return this
     */
    public JsonObjectBuilder and(String key, Object value) {
        pairs.add("\"" + JsonSupport.escape(key) + "\":" + formatValue(value));
        return this;
    }

    /**
     * JSONオブジェクト文字列を生成する。
     *
     * @return {"key":"value",...} 形式の文字列
     */
    public String build() {
        return "{" + String.join(",", pairs) + "}";
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof JsonRaw) {
            return ((JsonRaw) value).get();
        }
        if (value instanceof String) {
            return "\"" + JsonSupport.escape((String) value) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof List<?> list) {
            var strings = list.stream().map(Object::toString).toList();
            return JsonSupport.toJsonStringList(strings);
        }
        return "\"" + JsonSupport.escape(value.toString()) + "\"";
    }
}
