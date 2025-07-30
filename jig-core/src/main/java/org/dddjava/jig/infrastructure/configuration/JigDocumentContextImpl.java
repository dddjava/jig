package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeId;
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
    public Term packageTerm(PackageId packageId) {
        return glossaryRepository.get(packageId);
    }

    @Override
    public Term typeTerm(TypeId typeId) {
        return glossaryRepository.get(typeId);
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
                properties.outputDiagramTimeout,
                properties.diagramTransitiveReduction
        );
    }
}
