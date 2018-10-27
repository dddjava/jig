package org.dddjava.jig.presentation.view.graphvizj;

import org.dddjava.jig.presentation.view.DocumentSuffix;

class DotText {
    DocumentSuffix documentSuffix;
    String text;

    public DotText(DocumentSuffix documentSuffix, String text) {
        this.documentSuffix = documentSuffix;
        this.text = text;
    }

    public DotText(String text) {
        this(new DocumentSuffix(""), text);
    }

    String text() {
        if (isEmpty()) throw new NullPointerException();
        return text;
    }

    DocumentSuffix documentSuffix() {
        if (isEmpty()) throw new NullPointerException();
        return documentSuffix;
    }

    boolean isEmpty() {
        return text == null;
    }

    static DotText empty() {
        return new DotText(null);
    }
}
