package org.dddjava.jig.domain.model.documents.stationery;

import java.util.List;
import java.util.function.Consumer;

@Deprecated(since = "2026.3.1")
public record DiagramSources(List<DiagramSource> list) {

    public static DiagramSources of(List<DiagramSource> diagramSources) {
        return new DiagramSources(diagramSources);
    }

    public boolean noEntity() {
        return list.isEmpty();
    }

    public void each(Consumer<DiagramSource> consumer) {
        list.forEach(consumer);
    }
}
