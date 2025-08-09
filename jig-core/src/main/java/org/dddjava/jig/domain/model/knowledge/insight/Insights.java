package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.TermKind;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public record Insights(Collection<MethodInsight> values, Glossary glossary) {
    public List<PackageInsight> packageInsightList() {
        return typeInsightStream()
                .collect(groupingBy(typeInsight -> typeInsight.typeId().packageId(), toList()))
                .entrySet()
                .stream()
                .map(entry -> new PackageInsight(entry.getKey(), glossary.termOf(entry.getKey().asText(), TermKind.パッケージ), entry.getValue()))
                .sorted(Comparator.comparing(PackageInsight::fqn))
                .toList();
    }

    public List<TypeInsight> typeInsightList() {
        return typeInsightStream()
                .sorted(Comparator.comparing(TypeInsight::fqn))
                .toList();
    }

    private Stream<TypeInsight> typeInsightStream() {
        return values.stream()
                .collect(groupingBy(MethodInsight::typeId, toList()))
                .entrySet()
                .stream()
                .map(entry -> new TypeInsight(entry.getKey(), glossary.termOf(entry.getKey().value(), TermKind.クラス), entry.getValue()));
    }

    public List<MethodInsight> methodInsightList() {
        return values.stream()
                .sorted(Comparator.comparing(MethodInsight::fqn))
                .toList();
    }
}
