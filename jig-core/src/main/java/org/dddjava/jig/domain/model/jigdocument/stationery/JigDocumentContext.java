package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.models.jigobject.package_.JigPackage;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.package_.PackageComment;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

public interface JigDocumentContext {

    String label(String key);

    DocumentName documentName(JigDocument jigDocument);

    LinkPrefix linkPrefix();

    PackageComment packageComment(PackageIdentifier packageIdentifier);

    ClassComment classComment(TypeIdentifier typeIdentifier);

    default JigPackage jigPackage(PackageIdentifier packageIdentifier) {
        return new JigPackage(packageIdentifier, packageComment(packageIdentifier));
    }
}
