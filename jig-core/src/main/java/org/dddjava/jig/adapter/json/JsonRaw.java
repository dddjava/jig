package org.dddjava.jig.adapter.json;

/**
 * エスケープせずそのまま挿入する生JSONを表すマーカー。
 * {@link Json#raw(String)} で生成し、{@link JsonObjectBuilder#and} に渡す。
 */
final class JsonRaw {

    private final String json;

    JsonRaw(String json) {
        this.json = json;
    }

    String get() {
        return json;
    }
}
