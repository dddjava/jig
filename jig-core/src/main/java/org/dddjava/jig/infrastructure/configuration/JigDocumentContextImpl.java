package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigDocumentContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class JigDocumentContextImpl implements JigDocumentContext {

    private final JigSettings settings;

    public JigDocumentContextImpl(JigSettings settings) {
        this.settings = settings;
    }

    @Override
    public Path outputDirectory() {
        return settings.outputDirectory();
    }

    @Override
    public List<JigDocument> jigDocuments() {
        return settings.documentTypes();
    }

    @Override
    public Locale locale() {
        return settings.locale();
    }
}
