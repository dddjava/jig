package org.dddjava.jig.adapter.thymeleaf;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSupportTest {

    @Test
    void escape_バックスラッシュをエスケープする() {
        assertEquals("\\\\", JsonSupport.escape("\\"));
    }

    @Test
    void escape_ダブルクォートをエスケープする() {
        assertEquals("\\\"", JsonSupport.escape("\""));
    }

    @Test
    void escape_復帰をエスケープする() {
        assertEquals("\\r", JsonSupport.escape("\r"));
    }

    @Test
    void escape_改行をエスケープする() {
        assertEquals("\\n", JsonSupport.escape("\n"));
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
