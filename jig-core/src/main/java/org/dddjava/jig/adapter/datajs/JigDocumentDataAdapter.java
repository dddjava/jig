package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.domain.model.information.JigRepository;

public interface JigDocumentDataAdapter {
    String variableName();

    String dataFileName();

    String buildJson(JigRepository jigRepository);
}
