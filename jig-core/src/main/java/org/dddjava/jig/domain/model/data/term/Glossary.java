package org.dddjava.jig.domain.model.data.term;

import org.dddjava.jig.adapter.excel.ReportItem;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Glossary {
    Collection<Term> terms;

    public Glossary(Collection<Term> terms) {
        this.terms = terms;
    }

    public List<Term> list() {
        return terms.stream()
                .sorted(Comparator.comparing(Term::title).thenComparing(term -> term.identifier().asText()))
                .collect(Collectors.toList());
    }

    public static List<ReportItem<Term>> reporter() {
        return List.of(
                ReportItem.ofString("用語（英名）", term -> term.identifier().simpleText()),
                ReportItem.ofString("用語", term -> term.title()),
                ReportItem.ofString("説明", term -> term.description()),
                ReportItem.ofString("種類", term -> term.termKind().name()),
                ReportItem.ofString("識別子", term -> term.identifier().asText())
        );
    }

    public Term typeTermOf(TypeIdentifier typeIdentifier) {
        TermIdentifier termIdentifier = new TermIdentifier(typeIdentifier.fullQualifiedName());
        return terms.stream()
                .filter(term -> term.termKind() == TermKind.クラス)
                .filter(term -> term.identifier().equals(termIdentifier))
                .findAny()
                // 用語として事前登録されていなくても、IDがあるということは用語として存在することになるので、生成して返す。
                .orElseGet(() -> Term.simple(termIdentifier, typeIdentifier.asSimpleName(), TermKind.クラス));
    }

    public Stream<Term> stream() {
        return terms.stream();
    }
}
