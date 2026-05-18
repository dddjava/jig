package org.dddjava.jig.domain.model.documents;

import java.util.Locale;

public record JigDocumentLabel(String japanese, String english) {

    public static JigDocumentLabel of(String japanese, String english) {
        return new JigDocumentLabel(japanese, english);
    }

    public String labelFor(Locale locale) {
        return locale.getLanguage().equals("en") ? english : japanese;
    }
}
