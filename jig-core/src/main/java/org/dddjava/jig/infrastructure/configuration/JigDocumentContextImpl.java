package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.CommentRepository;
import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;

import java.nio.file.Path;
import java.util.List;

public class JigDocumentContextImpl implements JigDocumentContext {

    private final CommentRepository commentRepository;
    private final JigProperties properties;

    public JigDocumentContextImpl(CommentRepository commentRepository, JigProperties properties) {
        this.commentRepository = commentRepository;
        this.properties = properties;
    }

    @Override
    public PackageComment packageComment(PackageIdentifier packageIdentifier) {
        return commentRepository.get(packageIdentifier);
    }

    @Override
    public ClassComment classComment(TypeIdentifier typeIdentifier) {
        return commentRepository.get(typeIdentifier);
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
    public JigDiagramFormat diagramFormat() {
        return properties.outputDiagramFormat;
    }
}
