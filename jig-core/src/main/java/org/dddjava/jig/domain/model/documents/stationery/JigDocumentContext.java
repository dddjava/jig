package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.module.JigPackage;

import java.nio.file.Path;
import java.util.List;

public interface JigDocumentContext {

    Term packageTerm(PackageIdentifier packageIdentifier);

    Term typeTerm(TypeIdentifier typeIdentifier);

    default JigPackage jigPackage(PackageIdentifier packageIdentifier) {
        return new JigPackage(packageIdentifier, packageTerm(packageIdentifier));
    }

    Path outputDirectory();

    List<JigDocument> jigDocuments();

    JigDiagramOption diagramOption();
}
