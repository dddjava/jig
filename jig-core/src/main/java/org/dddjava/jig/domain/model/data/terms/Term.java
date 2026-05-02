package org.dddjava.jig.domain.model.data.terms;

import org.jspecify.annotations.Nullable;

/**
 * 用語
 */
public record Term(TermId id,
                   String title,
                   String description,
                   TermKind termKind,
                   @Nullable TermOrigin origin) {
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

}
