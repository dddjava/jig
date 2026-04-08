package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigDocumentContext;

import java.nio.file.Path;
import java.util.List;

public class JigDocumentContextImpl implements JigDocumentContext {

    private final JigProperties properties;

    public JigDocumentContextImpl(JigProperties properties) {
        this.properties = properties;
    }

    @Override
    public Path outputDirectory() {
        return properties.outputDirectory;
    }

    @Override
    public List<JigDocument> jigDocuments() {
        return properties.jigDocuments;
    }
}
