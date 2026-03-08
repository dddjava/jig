package org.dddjava.jig.adapter.thymeleaf;

import java.util.List;

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
     * 文字列リストをJSON配列に変換する。{@link JsonObjectBuilder#and} に渡すとエスケープされずそのまま挿入される。
     *
     * @param values 文字列のリスト
     * @return JSON配列として挿入するための値（例: ["a","b"]）
     */
    public static Object array(List<String> values) {
        return new JsonRaw(JsonSupport.toJsonStringList(values));
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
