package org.dddjava.jig.domain.model.parts.term;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Terms {
    List<Term> terms;

    public Terms(List<Term> terms) {
        this.terms = terms;
    }

    public List<Term> list() {
        return terms.stream()
                .sorted(Comparator.comparing(Term::termKind).thenComparing(term -> term.identifier.asText()))
                .collect(Collectors.toList());
    }
}
