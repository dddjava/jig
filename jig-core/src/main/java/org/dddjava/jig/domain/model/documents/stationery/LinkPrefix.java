package org.dddjava.jig.domain.model.documents.stationery;

import java.util.Objects;

/**
 * リンク先
 */
public class LinkPrefix {
    public static final String DISABLE = "<disable>";

    private final String value;

    public LinkPrefix(String value) {
        this.value = value;
    }

    public static LinkPrefix disable() {
        return new LinkPrefix(DISABLE);
    }

    public boolean disabled() {
        return DISABLE.equals(value);
    }

    public String textValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkPrefix that = (LinkPrefix) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
