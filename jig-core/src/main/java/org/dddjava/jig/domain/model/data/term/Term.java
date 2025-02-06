package org.dddjava.jig.domain.model.data.term;

/**
 * 用語
 */
public record Term(TermIdentifier identifier,
                   String title,
                   String description,
                   TermKind termKind,
                   Object additionalInformation) {
    public Term(TermIdentifier identifier, String title, String description, TermKind termKind) {
        this(identifier, title, description.trim(), termKind, null);
    }

    public String textWithDelimiter(String delimiter) {
        if (description.isEmpty()) return title;
        return title + delimiter + description;
    }
}
