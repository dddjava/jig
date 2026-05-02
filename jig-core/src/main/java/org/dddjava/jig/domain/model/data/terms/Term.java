package org.dddjava.jig.domain.model.data.terms;

/**
 * 用語
 */
public record Term(TermId id,
                   String title,
                   String description,
                   TermKind termKind,
                   TermOrigin origin) {
    public Term {
        title = title.isEmpty() ? id.simpleText() : title;
    }

    public String simpleText() {
        return id.simpleText();
    }
}
