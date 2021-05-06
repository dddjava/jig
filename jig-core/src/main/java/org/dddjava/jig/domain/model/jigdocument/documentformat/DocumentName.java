package org.dddjava.jig.domain.model.jigdocument.documentformat;

public class DocumentName {

    JigDocument jigDocument;
    String fileName;
    String label;

    DocumentName(JigDocument jigDocument, String fileName) {
        this.jigDocument = jigDocument;
        this.fileName = fileName;
        this.label = jigDocument.label();
    }

    public static DocumentName of(JigDocument jigDocument) {
        return new DocumentName(jigDocument, jigDocument.fileName());
    }

    public DocumentName withSuffix(String suffix) {
        return new DocumentName(this.jigDocument, this.fileName + suffix);
    }

    public String withExtension(JigDiagramFormat jigDiagramFormat) {
        return fileName + jigDiagramFormat.extension();
    }

    public String fileName() {
        return fileName;
    }

    public String label() {
        return label;
    }
}
