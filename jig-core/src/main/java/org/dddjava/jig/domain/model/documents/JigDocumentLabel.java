package org.dddjava.jig.domain.model.documents;

import java.util.Locale;

public class JigDocumentLabel {
    private final String japanese;
    private final String english;

    private JigDocumentLabel(String japanese, String english) {
        this.japanese = japanese;
        this.english = english;
    }

    public static JigDocumentLabel of(String japanese, String english) {
        return new JigDocumentLabel(japanese, english);
    }

    public String labelFor(Locale locale) {
        return locale.getLanguage().equals("en") ? english : japanese;
    }
}
