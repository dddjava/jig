package org.dddjava.jig.domain.model.jigdocument;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DiagramSource {

    DocumentName documentName;
    String text;
    AdditionalText additionalText;
    List<DiagramSource> compositeDiagramSources;

    public DiagramSource(DocumentName documentName, String text) {
        this(documentName, text, AdditionalText.empty());
    }

    public DiagramSource(DocumentName documentName, String text, AdditionalText additionalText) {
        this.documentName = documentName;
        this.text = text;
        this.additionalText = additionalText;
        this.compositeDiagramSources = Collections.emptyList();
    }

    public DiagramSource(List<DiagramSource> diagramSources) {
        DiagramSource first = diagramSources.get(0);
        this.documentName = first.documentName;
        this.text = first.text;
        this.additionalText = first.additionalText;
        this.compositeDiagramSources = diagramSources;
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

    public static DiagramSource empty() {
        return new DiagramSource(null, null);
    }

    public void each(Consumer<DiagramSource> consumer) {
        if (compositeDiagramSources.isEmpty()) {
            consumer.accept(this);
        }
        compositeDiagramSources.forEach(consumer);
    }
}
