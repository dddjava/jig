package org.dddjava.jig.domain.model.information.domains.term;

import java.util.Objects;

/**
 * 用語の識別子
 */
public class TermIdentifier {

    final String value;

    public TermIdentifier(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermIdentifier that = (TermIdentifier) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String asText() {
        return value;
    }

    public String simpleText() {
        int methodStart = value.indexOf('#');
        if (methodStart == -1) {
            int lastDot = value.lastIndexOf('.');
            String temp = lastDot != -1 ? value.substring(lastDot + 1) : value;

            return temp;
        }

        int argStart = value.indexOf('(');
        if (argStart == -1) {
            // ないはず
            return value;
        }
        return value.substring(methodStart + 1, argStart);
    }
}
