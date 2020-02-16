package org.dddjava.jig.domain.model.jigdocument;

public class DocumentName {

    String value;

    DocumentName(String value) {
        this.value = value;
    }

    public static DocumentName of(JigDocument jigDocument) {
        return new DocumentName(jigDocument.fileName());
    }

    public static DocumentName of(JigDocument jigDocument, String suffix) {
        return new DocumentName(jigDocument.fileName() + suffix);
    }

    public String withExtension(String extension) {
        return value + "." + extension;
    }
}
