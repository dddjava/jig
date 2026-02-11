package org.dddjava.jig.infrastructure.configuration;

import java.nio.file.Paths;

public enum JigProperty {

    OUTPUT_DIRECTORY,
    OUTPUT_DIAGRAM_FORMAT,
    PATTERN_DOMAIN,
    ;

    public static String defaultOutputDirectory() {
        return Paths.get(System.getProperty("user.dir")).resolve(".jig").toAbsolutePath().toString();
    }

    public static String defaultOutputDiagramFormat() {
        return "SVG";
    }

    public static String defaultPatternDomain() {
        return ".+\\.domain\\.(model|type)\\..+";
    }



}
