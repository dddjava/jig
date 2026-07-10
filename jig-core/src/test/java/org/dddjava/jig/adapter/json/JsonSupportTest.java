package org.dddjava.jig.adapter.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSupportTest {

    static Stream<Arguments> escape_単一文字のエスケープ() {
        return Stream.of(
                Arguments.of("バックスラッシュ", "\\", "\\\\"),
                Arguments.of("ダブルクォート", "\"", "\\\""),
                Arguments.of("復帰", "\r", "\\r"),
                Arguments.of("改行", "\n", "\\n"),
                Arguments.of("タブ", "\t", "\\t"),
                Arguments.of("C0制御文字", "", "\\u0001"),
                Arguments.of("行区切り(U+2028)", " ", "\\u2028"),
                Arguments.of("段落区切り(U+2029)", " ", "\\u2029")
        );
    }

    @ParameterizedTest(name = "{0}をエスケープする")
    @MethodSource
    void escape_単一文字のエスケープ(String label, String input, String expected) {
        assertEquals(expected, JsonSupport.escape(input));
    }

    @Test
    void escape_複数文字を同時にエスケープする() {
        assertEquals("a\\\\b\\\"c\\r\\nd", JsonSupport.escape("a\\b\"c\r\nd"));
    }

    @Test
    void escape_エスケープ不要の文字はそのまま() {
        assertEquals("abc", JsonSupport.escape("abc"));
    }

    @Test
    void toJsonStringList_空リストは空配列になる() {
        assertEquals("[]", JsonSupport.toJsonStringList(List.of()));
    }

    @Test
    void toJsonStringList_単一要素() {
        assertEquals("[\"a\"]", JsonSupport.toJsonStringList(List.of("a")));
    }

    @Test
    void toJsonStringList_複数要素() {
        assertEquals("[\"a\",\"b\",\"c\"]", JsonSupport.toJsonStringList(List.of("a", "b", "c")));
    }

    @Test
    void toJsonStringList_エスケープが必要な文字を含む() {
        assertEquals("[\"a\\\"b\"]", JsonSupport.toJsonStringList(List.of("a\"b")));
    }

    @Test
    void mapToJson_空Mapは空オブジェクトになる() {
        assertEquals("{}", JsonSupport.mapToJson(Map.of()));
    }

    @Test
    void mapToJson_単一エントリ() {
        assertEquals("{\"key\":value}", JsonSupport.mapToJson(Map.of("key", "value")));
    }

    @Test
    void mapToJson_複数エントリ() {
        Map<String, String> map = Map.of("a", "1", "b", "2");
        String actual = JsonSupport.mapToJson(map);
        assertEquals(2, actual.split(",").length);
        assertEquals("{", actual.substring(0, 1));
        assertEquals("}", actual.substring(actual.length() - 1));
    }

    @Test
    void mapToJson_キーがエスケープされる() {
        assertEquals("{\"\\\"key\\\"\":value}", JsonSupport.mapToJson(Map.of("\"key\"", "value")));
    }
}
