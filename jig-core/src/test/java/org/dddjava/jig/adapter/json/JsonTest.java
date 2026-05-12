package org.dddjava.jig.adapter.json;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonTest {

    @Test
    void object_単一プロパティ() {
        assertEquals("{\"a\":\"b\"}", Json.object("a", "b").build());
    }

    @Test
    void object_複数プロパティ() {
        assertEquals("{\"a\":\"1\",\"b\":\"2\"}", Json.object("a", "1").and("b", "2").build());
    }

    @Test
    void object_数値はエスケープされない() {
        assertEquals("{\"n\":123}", Json.object("n", 123).build());
    }

    @Test
    void object_真偽値はエスケープされない() {
        assertEquals("{\"f\":true}", Json.object("f", true).build());
    }

    @Test
    void object_文字列は自動エスケープされる() {
        assertEquals("{\"s\":\"a\\\"b\"}", Json.object("s", "a\"b").build());
    }

    @Test
    void object_arrayは生JSONとして挿入される() {
        assertEquals("{\"arr\":[\"a\",\"b\"]}", Json.object("arr", Json.array(List.of("a", "b"))).build());
    }

    @Test
    void object_rawは生JSONとして挿入される() {
        assertEquals("{\"raw\":[1,2,3]}", Json.object("raw", Json.raw("[1,2,3]")).build());
    }

    @Test
    void object_Listは配列として挿入される() {
        assertEquals("{\"list\":[\"x\",\"y\"]}", Json.object("list", List.of("x", "y")).build());
    }

    @Test
    void array_文字列リストをJSON配列としてobjectに渡せる() {
        String actual = Json.object("items", Json.array(List.of("a", "b"))).build();
        assertEquals("{\"items\":[\"a\",\"b\"]}", actual);
    }

    @Test
    void array_順序ありコレクションは順序を保つ() {
        var values = new LinkedHashSet<String>();
        values.add("c");
        values.add("a");
        values.add("b");
        assertEquals("{\"v\":[\"c\",\"a\",\"b\"]}", Json.object("v", Json.array(values)).build());
    }

    @Test
    void array_順序なしコレクションは自然順序でソートする() {
        Set<String> values = new HashSet<>(List.of("c", "a", "b"));
        assertEquals("{\"v\":[\"a\",\"b\",\"c\"]}", Json.object("v", Json.array(values)).build());
    }

    @Test
    void arrayObjects_順序ありコレクションは順序を保つ() {
        var builders = List.of(Json.object("k", "c"), Json.object("k", "a"), Json.object("k", "b"));
        assertEquals("[{\"k\":\"c\"},{\"k\":\"a\"},{\"k\":\"b\"}]",
                ((JsonRaw) Json.arrayObjects(builders)).get());
    }

    @Test
    void arrayObjects_順序なしコレクションはJSON文字列でソートする() {
        Set<JsonObjectBuilder> builders = new HashSet<>(List.of(
                Json.object("k", "c"), Json.object("k", "a"), Json.object("k", "b")));
        assertEquals("[{\"k\":\"a\"},{\"k\":\"b\"},{\"k\":\"c\"}]",
                ((JsonRaw) Json.arrayObjects(builders)).get());
    }
}
