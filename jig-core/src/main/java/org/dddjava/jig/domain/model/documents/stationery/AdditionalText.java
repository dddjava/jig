package org.dddjava.jig.domain.model.documents.stationery;

public record AdditionalText(String value) {

    public static AdditionalText empty() {
        return new AdditionalText(null);
    }

    public boolean enable() {
        return value != null;
    }
}
