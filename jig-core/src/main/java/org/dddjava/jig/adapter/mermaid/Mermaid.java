package org.dddjava.jig.adapter.mermaid;

/**
 * Mermaid表記
 *
 * 出力する際に必要なエスケープなどを共通化する
 */
enum Mermaid {
    ;

    public static String box(String id, String label) {
        var escapedLabel = label.replace("\"", "#quote;").replace("<", "&lt;").replace(">", "&gt;");
        return "%s[\"%s\"]".formatted(id, escapedLabel);
    }
}
