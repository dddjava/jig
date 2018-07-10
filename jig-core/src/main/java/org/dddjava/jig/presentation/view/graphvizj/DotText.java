package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.presentation.view.DocumentSuffix;

public class DotText {
    DocumentSuffix documentSuffix;
    String text;

    public DotText(DocumentSuffix documentSuffix, String text) {
        this.documentSuffix = documentSuffix;
        this.text = text;
    }

    public DotText(String text) {
        this(new DocumentSuffix(""), text);
    }

    public String text() {
        return text;
    }

    public DocumentSuffix documentSuffix() {
        return documentSuffix;
    }
}
