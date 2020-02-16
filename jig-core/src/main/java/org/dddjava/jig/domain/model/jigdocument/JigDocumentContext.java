package org.dddjava.jig.domain.model.jigdocument;

public interface JigDocumentContext {

    String label(String key);

    DocumentName documentName(JigDocument jigDocument);
}
