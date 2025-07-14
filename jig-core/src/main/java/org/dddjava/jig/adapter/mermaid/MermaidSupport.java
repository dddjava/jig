package org.dddjava.jig.adapter.mermaid;

import org.dddjava.jig.adapter.html.HtmlSupport;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;

public class MermaidSupport {
    static String mermaidIdText(JigMethodId jigMethodId) {
        return HtmlSupport.htmlMethodIdText(jigMethodId);
    }

    public static String box(String id, String label) {
        var escapedLabel = label.replace("\"", "#quote;").replace("<", "&lt;").replace(">", "&gt;");
        return "%s[\"%s\"]".formatted(id, escapedLabel);
    }
}
