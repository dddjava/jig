package org.dddjava.jig.infrastructure.configuration;

import java.util.Objects;

public class OutputOmitPrefix {
    String pattern;

    public OutputOmitPrefix(String pattern) {
        this.pattern = pattern;
    }

    public String format(String fullQualifiedName) {
        return fullQualifiedName.replaceFirst(pattern, "");
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputOmitPrefix that = (OutputOmitPrefix) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}
