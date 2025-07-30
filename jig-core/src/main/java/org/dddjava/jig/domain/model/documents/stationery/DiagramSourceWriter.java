package org.dddjava.jig.domain.model.documents.stationery;

import java.util.function.Consumer;

/**
 * DiagramSourcesを出力できる
 */
public interface DiagramSourceWriter {

    default DiagramSources sources(JigDocumentContext jigDocumentContext) {
        return sources();
    }

    default DiagramSources sources() {
        throw new UnsupportedOperationException();
    }

    default int write(Consumer<DiagramSource> diagramSourceWriteProcess) {
        // no-op
        return -1;
    }
}
