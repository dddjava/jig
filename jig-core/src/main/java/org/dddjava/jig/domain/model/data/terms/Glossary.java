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

}
