package org.dddjava.jig.adapter.html.mermaid;

enum Mermaid {
    BOX;

    String of(String id, String label) {
        var escapedLabel = label.replace("\"", "#quote;").replace("<", "&lt;").replace(">", "&gt;");
        return "%s[\"%s\"]".formatted(id, escapedLabel);
    }
}
