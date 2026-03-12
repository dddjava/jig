package org.dddjava.jig.adapter.thymeleaf;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSONオブジェクトを組み立てるビルダー。
 * {@link Json#object(String, Object)} で開始し、{@link #and} でプロパティを追加する。
 */
public final class JsonObjectBuilder {

    // JSONオブジェクトのキーは重複させず、後勝ちで上書きする（呼び出し側がindex的に使えるようにする）
    private final Map<String, String> pairs = new LinkedHashMap<>();

    JsonObjectBuilder() {
    }

    /**
     * プロパティを追加する。文字列は自動エスケープされる。
     * キーが重複した場合は後勝ちで上書きする。
     *
     * @param key   プロパティ名
     * @param value プロパティ値（String, Number, Boolean, JsonRaw, List&lt;String&gt; など）
     * @return this
     */
    public JsonObjectBuilder and(String key, Object value) {
        pairs.put(JsonSupport.escape(key), formatValue(value));
        return this;
    }

    /**
     * JSONオブジェクト文字列を生成する。
     *
     * @return {"key":"value",...} 形式の文字列
     */
    public String build() {
        return pairs.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":" + e.getValue())
                .reduce((a, b) -> a + "," + b)
                .map(content -> "{" + content + "}")
                .orElse("{}");
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof JsonRaw) {
            return ((JsonRaw) value).get();
        }
        if (value instanceof JsonObjectBuilder builder) {
            return builder.build();
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
