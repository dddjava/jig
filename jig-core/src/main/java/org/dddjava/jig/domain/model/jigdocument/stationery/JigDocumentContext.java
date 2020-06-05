package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;

public interface JigDocumentContext {

    String label(String key);

    DocumentName documentName(JigDocument jigDocument);
}
