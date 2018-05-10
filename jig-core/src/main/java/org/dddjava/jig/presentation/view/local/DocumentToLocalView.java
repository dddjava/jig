package org.dddjava.jig.presentation.view.local;

import org.dddjava.jig.domain.model.DocumentType;

public interface DocumentToLocalView {

    LocalView convert(DocumentType documentType);
}
