package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.application.JigRepository;

public interface DataAdapter {
    String variableName();

    String dataFileName();

    String buildJson(JigRepository jigRepository);
}
