package org.dddjava.jig.infrastructure.configuration;

import java.nio.file.Paths;

public enum JigProperty {

    OUTPUT_DIRECTORY("") {
        @Override
        String defaultValue() {
            return Paths.get(System.getProperty("user.dir")).resolve(".jig").toAbsolutePath().toString();
        }
    },
    OUTPUT_DIAGRAM_FORMAT("SVG"),

    PATTERN_DOMAIN(".+\\.domain\\.(model|type)\\..+"),
    ;

    private final String defaultValue;

    JigProperty(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    String defaultValue() {
        return defaultValue;
    }
}
