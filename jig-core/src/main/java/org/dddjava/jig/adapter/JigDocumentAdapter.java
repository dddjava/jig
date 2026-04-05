package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.information.JigRepository;

public interface JigDocumentAdapter {
    String variableName();

    String dataFileName();

    String buildJson(JigRepository jigRepository);
}
