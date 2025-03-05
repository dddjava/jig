package org.dddjava.jig.domain.model.data.term;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用語集
 */
public record Glossary(Collection<Term> terms) {

    public List<Term> list() {
        return terms.stream()
                .sorted(Comparator.comparing(Term::title).thenComparing(term -> term.identifier().asText()))
                .collect(Collectors.toList());
    }

    public Collection<Term> findRelated(TermIdentifier termIdentifier) {
        return terms.stream()
                .filter(term -> term.relatesTo(termIdentifier))
                .toList();
    }
}
