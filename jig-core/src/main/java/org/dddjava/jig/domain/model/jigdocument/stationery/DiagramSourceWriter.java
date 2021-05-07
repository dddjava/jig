package org.dddjava.jig.domain.model.jigdocument.stationery;

/**
 * DiagramSourcesを出力できる
 */
public interface DiagramSourceWriter {

    DiagramSources sources(JigDocumentContext jigDocumentContext);
}
