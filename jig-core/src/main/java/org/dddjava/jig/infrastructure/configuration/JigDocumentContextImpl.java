package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;

import java.nio.file.Path;
import java.util.List;

public class JigDocumentContextImpl implements JigDocumentContext {

    private final GlossaryRepository glossaryRepository;
    private final JigProperties properties;

    public JigDocumentContextImpl(GlossaryRepository glossaryRepository, JigProperties properties) {
        this.glossaryRepository = glossaryRepository;
        this.properties = properties;
    }

    @Override
    public Term packageTerm(PackageIdentifier packageIdentifier) {
        return glossaryRepository.get(packageIdentifier);
    }

    @Override
    public Term typeTerm(TypeIdentifier typeIdentifier) {
        return glossaryRepository.get(typeIdentifier);
    }

    @Override
    public Path outputDirectory() {
        return properties.outputDirectory;
    }

    @Override
    public List<JigDocument> jigDocuments() {
        return properties.jigDocuments;
    }

    @Override
    public JigDiagramOption diagramOption() {
        return new JigDiagramOption(
                properties.outputDiagramFormat,
                properties.diagramTransitiveReduction
        );
    }
}
