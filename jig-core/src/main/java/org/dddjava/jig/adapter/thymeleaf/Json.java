package org.dddjava.jig.adapter.thymeleaf;

import java.util.LinkedHashMap;
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
     * キー付きのJSON断片を蓄積し、JSONオブジェクトとして埋め込むためのビルダーを作る。
     * <p>
     * 値は「すでに組み立て済みのJSON断片」として扱う。
     */
    public static ObjectIndex objectIndex() {
        return new ObjectIndex();
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
     * 文字列リストをJSON配列に変換する。{@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param values 文字列のリスト
     * @return JSON配列として挿入するための値（例: ["a","b"]）
     */
    public static Object array(List<String> values) {
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
     * JSONオブジェクトビルダーのリストをJSON配列としてそのまま挿入する。
     * {@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param builders JSONオブジェクトビルダー
     * @return JSON配列として挿入するための値（例: [{"a":1},{"b":2}]）
     */
    public static Object arrayObjects(List<JsonObjectBuilder> builders) {
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

    /**
     * JSON断片をキーで管理するオブジェクトインデックス。
     * Mapの利用を呼び出し側から隠し、最終的に {@link #asObject()} で埋め込み可能な値を返す。
     */
    public static final class ObjectIndex {
        private final Map<String, String> map = new LinkedHashMap<>();

        ObjectIndex() {
        }

        /**
         * JSON断片を登録する。キーが重複した場合は後勝ちで上書きする。
         */
        public ObjectIndex put(String key, String jsonFragment) {
            map.put(key, jsonFragment);
            return this;
        }

        /**
         * JSONオブジェクトビルダーを登録する。キーが重複した場合は後勝ちで上書きする。
         */
        public ObjectIndex put(String key, JsonObjectBuilder builder) {
            map.put(key, builder.build());
            return this;
        }

        /**
         * {@link JsonObjectBuilder#and} に渡せる「生JSONオブジェクト」として返す。
         */
        public Object asObject() {
            return Json.object(map);
        }
    }
}
