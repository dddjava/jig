package org.dddjava.jig.domain.model.models.domains.term;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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

    public static List<Map.Entry<String, Function<Term, Object>>> reporter() {
        return List.of(
                Map.entry("用語（英名）", term -> term.identifier().simpleText()),
                Map.entry("用語", term -> term.title()),
                Map.entry("説明", term -> term.description()),
                Map.entry("種類", term -> term.termKind().name()),
                Map.entry("識別子", term -> term.identifier().asText())
        );
    }
}
