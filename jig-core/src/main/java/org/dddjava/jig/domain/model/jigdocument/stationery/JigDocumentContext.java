package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.parts.alias.AliasFinder;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

public interface JigDocumentContext {

    String label(String key);

    DocumentName documentName(JigDocument jigDocument);

    AliasFinder aliasFinder();

    default String aliasTextOrDefault(TypeIdentifier handlerType, String defaultText) {
        return aliasFinder().find(handlerType).asTextOrDefault(defaultText);
    };

    LinkPrefix linkPrefix();
}
