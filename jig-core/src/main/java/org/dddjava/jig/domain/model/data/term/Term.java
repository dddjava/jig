package org.dddjava.jig.domain.model.data.term;

/**
 * 用語
 */
public record Term(TermIdentifier identifier,
                   String title,
                   String description,
                   TermKind termKind,
                   Object additionalInformation) {
    public Term {
        title = title.isEmpty() ? identifier.simpleText() : title;
    }

    public Term(TermIdentifier identifier, String title, String description, TermKind termKind) {
        this(identifier, title, description.trim(), termKind, null);
    }

    public static Term simple(TermIdentifier termIdentifier, String title, TermKind termKind) {
        return new Term(termIdentifier, title, "", termKind);
    }

    public String titleAndSimpleName(String delimiter) {
        String identifierSimpleText = identifier.simpleText();
        if (title.isEmpty() || title.equals(identifierSimpleText)) return identifierSimpleText;
        return title + delimiter + identifierSimpleText;
    }

    /**
     * 用語の関連をIDで判定する
     *
     * 前方一致していれば関連していると見做す
     */
    public boolean relatesTo(String otherIdentifier) {
        return identifier().asText().startsWith(otherIdentifier);
    }
}
