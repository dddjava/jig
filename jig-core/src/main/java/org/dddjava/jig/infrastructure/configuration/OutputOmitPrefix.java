package org.dddjava.jig.infrastructure.configuration;

public class OutputOmitPrefix {
    String pattern;

    public OutputOmitPrefix(String pattern) {
        this.pattern = pattern;
    }

    public String format(String fullQualifiedName) {
        return fullQualifiedName.replaceFirst(pattern, "");
    }
}
