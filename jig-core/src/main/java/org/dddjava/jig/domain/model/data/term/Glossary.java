package org.dddjava.jig.domain.model.data.term;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.infrastructure.javaparser.TermFactory;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Glossary {
    Collection<Term> terms;

    public Glossary(Collection<Term> terms) {
        this.terms = terms;
    }

    public List<Term> list() {
        return terms.stream()
                .sorted(Comparator.comparing(Term::termKind).thenComparing(term -> term.identifier().asText()))
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

    public Term typeTermOf(TypeIdentifier typeIdentifier) {
        TermIdentifier termIdentifier = new TermIdentifier(typeIdentifier.fullQualifiedName());
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> TermFactory.fromClass(termIdentifier, typeIdentifier.asSimpleText()));
    }

    public Stream<Term> stream() {
        return terms.stream();
    }
}
