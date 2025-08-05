package org.dddjava.jig.domain.model.documents.stationery;

import org.jspecify.annotations.Nullable;

public record AdditionalText(@Nullable String value) {

    public static AdditionalText empty() {
        return new AdditionalText(null);
    }

    public boolean enable() {
        return value != null;
    }
}
