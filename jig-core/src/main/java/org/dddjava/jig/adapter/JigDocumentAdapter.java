package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.JigRepository;

import java.nio.file.Path;
import java.util.List;

public interface JigDocumentAdapter {
    JigDocument supportedDocument();

    List<Path> write(JigDocument jigDocument, JigRepository jigRepository);
}
