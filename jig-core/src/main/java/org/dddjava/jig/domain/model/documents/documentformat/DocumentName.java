package org.dddjava.jig.domain.model.documents.documentformat;

public record DocumentName(JigDocument jigDocument, String fileName, String label) {

    private static DocumentName of(JigDocument jigDocument, String fileName) {
        return new DocumentName(jigDocument, fileName, jigDocument.label());
    }

    public static DocumentName of(JigDocument jigDocument) {
        return of(jigDocument, jigDocument.fileName());
    }

    public DocumentName withSuffix(String suffix) {
        return of(this.jigDocument, this.fileName + suffix);
    }

    public String withExtension(JigDiagramFormat jigDiagramFormat) {
        return fileName + jigDiagramFormat.extension();
    }
}
