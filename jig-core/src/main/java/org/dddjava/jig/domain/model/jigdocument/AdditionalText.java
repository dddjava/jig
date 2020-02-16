package org.dddjava.jig.domain.model.jigdocument;

public class AdditionalText {
    String value;

    public AdditionalText(String value) {
        this.value = value;
    }

    public static AdditionalText empty() {
        return new AdditionalText(null);
    }

    public String value() {
        return value;
    }

    public boolean enable() {
        return value != null;
    }
}
