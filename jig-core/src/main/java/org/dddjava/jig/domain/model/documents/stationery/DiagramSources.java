package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public record DiagramSources(List<DiagramSource> list) {

    public static DiagramSources empty() {
        return new DiagramSources(Collections.emptyList());
    }

    public static DiagramSources singleDiagramSource(DocumentName documentName, String text) {
        return new DiagramSources(Collections.singletonList(new DiagramSource(documentName, text, AdditionalText.empty())));
    }

    public boolean noEntity() {
        return list.isEmpty();
    }

    public void each(Consumer<DiagramSource> consumer) {
        list.forEach(consumer);
    }
}
