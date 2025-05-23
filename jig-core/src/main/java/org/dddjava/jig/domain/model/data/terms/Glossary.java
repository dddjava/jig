package org.dddjava.jig.domain.model.data.terms;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 用語集
 */
public record Glossary(Collection<Term> terms) {

    public List<Term> list() {
        return terms.stream()
                .sorted(Comparator.comparing(Term::title).thenComparing(term -> term.identifier().asText()))
                .toList();
    }

    public Collection<Term> findRelated(TermIdentifier termIdentifier) {
        return terms.stream()
                .filter(term -> term.relatesTo(termIdentifier))
                .toList();
    }

    public Term termOf(String idText, TermKind termKind) {
        TermIdentifier termIdentifier = new TermIdentifier(idText);
        return terms.stream()
                .filter(term -> term.termKind() == termKind)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                .orElseGet(() -> Term.simple(termIdentifier, termIdentifier.simpleText(), termKind));
    }
}
