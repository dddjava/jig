package org.dddjava.jig.domain.model.parts.term;

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
}
