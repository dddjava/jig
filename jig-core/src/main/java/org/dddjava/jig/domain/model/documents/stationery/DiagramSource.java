package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;

import java.util.Collections;
import java.util.List;

public record DiagramSource(DocumentName documentName, String text, AdditionalText additionalText) {

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

    public static DiagramSource createDiagramSourceUnit(DocumentName documentName, String text) {
        return new DiagramSource(documentName, text, AdditionalText.empty());
    }

    @Override
    public String text() {
        if (noValue()) throw new NullPointerException();
        return text;
    }

    public boolean noValue() {
        return text == null;
    }
}
