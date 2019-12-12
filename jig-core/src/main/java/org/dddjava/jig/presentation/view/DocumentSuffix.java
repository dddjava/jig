package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.model.jigdocument.JigDocument;

public class DocumentSuffix {
    String name;

    public DocumentSuffix(String name) {
        this.name = name;
    }

    public String withFileNameOf(JigDocument jigDocument) {
        return jigDocument.fileName() + name;
    }
}
