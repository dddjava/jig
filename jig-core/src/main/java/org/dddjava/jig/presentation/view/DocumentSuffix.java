package org.dddjava.jig.presentation.view;

public class DocumentSuffix {
    String name;

    public DocumentSuffix(String name) {
        this.name = name;
    }

    public String withFileNameOf(JigDocument jigDocument) {
        return jigDocument.fileName() + name;
    }
}
