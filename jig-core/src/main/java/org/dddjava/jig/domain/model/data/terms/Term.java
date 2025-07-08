package org.dddjava.jig.domain.model.data.terms;

/**
 * 用語
 */
public record Term(TermId id,
                   String title,
                   String description,
                   TermKind termKind,
                   Object additionalInformation) {
    public Term {
        title = title.isEmpty() ? id.simpleText() : title;
    }

    public Term(TermId identifier, String title, String description, TermKind termKind) {
        this(identifier, title, description.trim(), termKind, null);
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

    /**
     * 用語の関連をIDで判定する
     *
     * 前方一致していれば関連していると見做す
     */
    public boolean relatesTo(TermId otherIdentifier) {
        return id().asText().startsWith(otherIdentifier.asText());
    }
}
