package org.dddjava.jig.domain.model.documents.stationery;

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
}
