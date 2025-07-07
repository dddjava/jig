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
                .sorted(Comparator.comparing(Term::title).thenComparing(term -> term.id().asText()))
                .toList();
    }

    public Collection<Term> findRelated(TermId termId) {
        return terms.stream()
                .filter(term -> term.relatesTo(termId))
                .toList();
    }

    public Term termOf(String idText, TermKind termKind) {
        TermId termId = new TermId(idText);
        return terms.stream()
                .filter(term -> term.termKind() == termKind)
                .filter(term -> term.id().equals(termId))
                .findAny()
                .orElseGet(() -> Term.simple(termId, termId.simpleText(), termKind));
    }
}
