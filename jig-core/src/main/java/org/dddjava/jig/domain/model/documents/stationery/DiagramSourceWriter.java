package org.dddjava.jig.domain.model.documents.stationery;

/**
 * DiagramSourcesを出力できる
 */
public interface DiagramSourceWriter {

    DiagramSources sources(JigDocumentContext jigDocumentContext);
}
