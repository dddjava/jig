package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.classes.type.ClassComment;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageComment;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.information.jigobject.package_.JigPackage;

import java.nio.file.Path;
import java.util.List;

public interface JigDocumentContext {

    PackageComment packageComment(PackageIdentifier packageIdentifier);

    ClassComment classComment(TypeIdentifier typeIdentifier);

    default JigPackage jigPackage(PackageIdentifier packageIdentifier) {
        return new JigPackage(packageIdentifier, packageComment(packageIdentifier));
    }

    Path outputDirectory();

    List<JigDocument> jigDocuments();

    JigDiagramFormat diagramFormat();
}
