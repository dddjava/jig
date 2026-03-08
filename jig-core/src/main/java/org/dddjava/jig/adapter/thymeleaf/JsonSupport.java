package org.dddjava.jig.adapter.thymeleaf;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thymeleafアダプター向けのJSON文字列生成ユーティリティ。
 * ドキュメント用のインラインJSONを組み立てる際の共通処理を提供する。
 */
public final class JsonSupport {

    private JsonSupport() {
    }

    /**
     * JSON文字列内の特殊文字をエスケープする。
     *
     * @param string エスケープ対象の文字列
     * @return エスケープ済みの文字列
     */
    public static String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    /**
     * 文字列リストをJSON配列文字列に変換する。
     *
     * @param values 文字列のリスト
     * @return JSON配列形式の文字列（例: ["a","b"]）
     */
    public static String toJsonStringList(List<String> values) {
        return values.stream()
                .map(JsonSupport::escape)
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    /**
     * キーが文字列・値がJSON断片のMapをJSONオブジェクト文字列に変換する。
     *
     * @param map キーとJSON断片のマップ
     * @return JSONオブジェクト形式の文字列（例: {"key1":value1,"key2":value2}）
     */
    public static String mapToJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"%s\":%s".formatted(escape(e.getKey()), e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }
}
