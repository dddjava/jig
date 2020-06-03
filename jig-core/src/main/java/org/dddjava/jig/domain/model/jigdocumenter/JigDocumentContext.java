package org.dddjava.jig.domain.model.jigdocumenter;

import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;

public interface JigDocumentContext {

    String label(String key);

    DocumentName documentName(JigDocument jigDocument);
}
