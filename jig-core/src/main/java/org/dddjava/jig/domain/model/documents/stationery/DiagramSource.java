package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;

@Deprecated(since = "2026.3.1")
public record DiagramSource(DocumentName documentName, String text, AdditionalText additionalText) {

    public static DiagramSource createDiagramSourceUnit(DocumentName documentName, String text, AdditionalText additionalText) {
        return new DiagramSource(documentName, text, additionalText);
    }

    public static DiagramSource createDiagramSourceUnit(DocumentName documentName, String text) {
        return new DiagramSource(documentName, text, AdditionalText.empty());
    }

    @Override
    public String text() {
        return text;
    }
}
