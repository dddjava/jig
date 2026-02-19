package org.dddjava.jig.domain.model.documents.stationery;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public record AdditionalText(@Nullable String value) {

    public static AdditionalText empty() {
        return new AdditionalText(null);
    }

    public void ifPresent(Consumer<String> consumer) {
        if (value == null) return;
        consumer.accept(value);
    }
}
