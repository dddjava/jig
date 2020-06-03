package org.dddjava.jig.domain.model.jigdocumenter.stationery;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DiagramSources {
    List<DiagramSource> diagramSources;

    public DiagramSources(List<DiagramSource> diagramSources) {
        this.diagramSources = diagramSources;
    }

    public static DiagramSources empty() {
        return new DiagramSources(Collections.emptyList());
    }

    public boolean noEntity() {
        return diagramSources.isEmpty();
    }

    public void each(Consumer<DiagramSource> consumer) {
        diagramSources.forEach(consumer);
    }
}
