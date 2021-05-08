package org.dddjava.jig.domain.model.documents.stationery;

/**
 * リンク先
 */
public class LinkPrefix {
    public static final String DISABLE = "<disable>";

    private String value;

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
}
