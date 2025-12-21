package org.dddjava.jig.domain.model.data.terms;

import org.jspecify.annotations.Nullable;

/**
 * 用語
 */
public record Term(TermId id,
                   String title,
                   String description,
                   TermKind termKind,
                   @Nullable Object additionalInformation) {
    public Term {
        title = title.isEmpty() ? id.simpleText() : title;
    }

    public Term(TermId termId, String title, String description, TermKind termKind) {
        this(termId, title, description.trim(), termKind, null);
    }

    public static Term simple(TermId termId, String title, TermKind termKind) {
        return new Term(termId, title, "", termKind);
    }

    public String simpleText() {
        return id.simpleText();
    }

    public String titleAndSimpleName(String delimiter) {
        String simpleText = simpleText();
        if (title.isEmpty() || title.equals(simpleText)) return simpleText;
        return title + delimiter + simpleText;
    }

    public boolean hasAlias() {
        return !simpleText().equals(title);
    }

    /**
     * 用語の関連をIDで判定する
     *
     * 前方一致していれば関連していると見做す
     */
    public boolean relatesTo(TermId otherTermId) {
        return id.asText().startsWith(otherTermId.asText());
    }
}
