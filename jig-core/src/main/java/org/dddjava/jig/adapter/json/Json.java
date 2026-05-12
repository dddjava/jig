package org.dddjava.jig.adapter.json;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 簡易JSON組み立てAPI。
 * 呼び出し元でescapeを書かずにfluentにJSONオブジェクトを組み立てられる。
 */
public final class Json {

    private Json() {
    }

    /**
     * 空のJSONオブジェクトの組み立てを開始する。
     *
     * @return ビルダー
     */
    public static JsonObjectBuilder object() {
        return new JsonObjectBuilder();
    }

    /**
     * JSONオブジェクトの組み立てを開始する。
     *
     * @param key   最初のプロパティ名
     * @param value 最初のプロパティ値（Stringは自動エスケープされる）
     * @return ビルダー
     */
    public static JsonObjectBuilder object(String key, Object value) {
        return new JsonObjectBuilder().and(key, value);
    }

    /**
     * 値がJSON断片（エスケープ不要）になっているMapを、JSONオブジェクトとしてそのまま挿入する。
     * {@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param map キーとJSON断片のマップ
     * @return JSONオブジェクトとして挿入するための値（例: {"key1":value1,"key2":value2}）
     */
    public static Object object(Map<String, String> map) {
        return new JsonRaw(JsonSupport.mapToJson(map));
    }

    /**
     * 文字列コレクションをJSON配列に変換する。{@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param values 文字列のコレクション
     * @return JSON配列として挿入するための値（例: ["a","b"]）
     */
    public static Object array(Collection<String> values) {
        return new JsonRaw(JsonSupport.toJsonStringList(values));
    }

    /**
     * JSON断片のリストをJSON配列としてそのまま挿入する。要素はすでにJSONとして組み立て済みであること。
     * {@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param jsonFragments すでに組み立て済みのJSON断片（例: {"a":1} や 123）
     * @return JSON配列として挿入するための値（例: [{"a":1},123]）
     */
    public static Object arrayRaw(List<String> jsonFragments) {
        return new JsonRaw("[" + String.join(",", jsonFragments) + "]");
    }

    /**
     * JSONオブジェクトビルダーのコレクションをJSON配列としてそのまま挿入する。
     * {@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param builders JSONオブジェクトビルダー
     * @return JSON配列として挿入するための値（例: [{"a":1},{"b":2}]）
     */
    public static Object arrayObjects(Collection<JsonObjectBuilder> builders) {
        String json = builders.stream()
                .map(JsonObjectBuilder::build)
                .collect(Collectors.joining(",", "[", "]"));
        return new JsonRaw(json);
    }

    /**
     * 生JSONをそのまま挿入する。{@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param json すでに組み立て済みのJSON断片（例: [1,2,3] や {"nested":"value"}）
     * @return 挿入用の値
     */
    public static Object raw(String json) {
        return new JsonRaw(json);
    }
}
