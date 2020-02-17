package org.dddjava.jig.domain.model.jigdocument;

import java.util.Collections;
import java.util.List;

public class DiagramSource {

    DocumentName documentName;
    String text;
    AdditionalText additionalText;

    private DiagramSource(DocumentName documentName, String text, AdditionalText additionalText) {
        this.documentName = documentName;
        this.text = text;
        this.additionalText = additionalText;
    }

    public static DiagramSources createDiagramSource(DocumentName documentName, String text) {
        return createDiagramSource(documentName, text, AdditionalText.empty());
    }

    public static DiagramSources createDiagramSource(DocumentName documentName, String text, AdditionalText additionalText) {
        return new DiagramSources(Collections.singletonList(new DiagramSource(documentName, text, additionalText)));
    }

    public static DiagramSources createDiagramSource(List<DiagramSource> diagramSources) {
        return new DiagramSources(diagramSources);
    }

    public static DiagramSource createDiagramSourceUnit(DocumentName documentName, String text, AdditionalText additionalText) {
        return new DiagramSource(documentName, text, additionalText);
    }

    public static DiagramSource emptyUnit() {
        return new DiagramSource(null, null, AdditionalText.empty());
    }

    public String text() {
        if (noValue()) throw new NullPointerException();
        return text;
    }

    public AdditionalText additionalText() {
        return additionalText;
    }

    public DocumentName documentName() {
        return documentName;
    }

    public boolean noValue() {
        return text == null;
    }

    public static DiagramSources empty() {
        return DiagramSources.empty();
    }
}
