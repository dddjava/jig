package org.dddjava.jig.domain.model.jigdocument;

import org.dddjava.jig.presentation.view.DocumentSuffix;
import org.dddjava.jig.presentation.view.JigDocumentWriter;

public class DotText {
    // FIXME DotTextがDocument名を持つのはおかしい
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
        if (isEmpty()) throw new NullPointerException();
        return text;
    }

    public DocumentSuffix documentSuffix() {
        if (isEmpty()) throw new NullPointerException();
        return documentSuffix;
    }

    public boolean isEmpty() {
        return text == null;
    }

    public static DotText empty() {
        return new DotText(null);
    }

    @Deprecated
    public void additionalWrite(JigDocumentWriter jigDocumentWriter) {
        // no-op
    }
}
