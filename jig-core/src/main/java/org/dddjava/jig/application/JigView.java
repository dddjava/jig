package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface JigView {

    default List<Path> write(Path outputDirectory, Object model) throws IOException {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument(), outputDirectory);
        render(model, jigDocumentWriter);
        return jigDocumentWriter.outputFilePaths();
    }

    JigDocument jigDocument();

    void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException;
}
