package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public record Insights(Collection<MethodInsight> values, Glossary glossary, TypeRelationships typeRelationships,
                       JigTypes jigTypes) {
    /**
     * クラスを直接持たない中間パッケージも、プロダクトのルートパッケージ以下であれば含める。
     * 一覧で階層の抜けがあると、どこまでが同じ塊なのかを読み違えるため。
     */
    public List<PackageInsight> packageInsightList() {
        Map<PackageId, List<TypeInsight>> typeInsightsByPackage = typeInsightStream()
                .collect(groupingBy(typeInsight -> typeInsight.typeId().packageId(), toList()));

        List<PackageId> namedPackages = typeInsightsByPackage.keySet().stream()
                .filter(packageId -> !packageId.equals(PackageId.defaultPackage()))
                .toList();
        Optional<PackageId> root = commonRootPackage(namedPackages);
        namedPackages.stream()
                .flatMap(packageId -> Stream.iterate(packageId.parentIfExist(), Optional::isPresent, parent -> parent.get().parentIfExist())
                        .map(Optional::get)
                        .takeWhile(parent -> root.map(rootPackage -> isSelfOrSubpackage(parent, rootPackage)).orElse(true)))
                .toList()
                .forEach(parent -> typeInsightsByPackage.putIfAbsent(parent, List.of()));

        return typeInsightsByPackage.entrySet()
                .stream()
                .map(entry -> new PackageInsight(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(PackageInsight::fqn))
                .toList();
    }

    /**
     * すべてのパッケージが自身かその配下になる、最も深いパッケージ。共通するものがなければ空。
     */
    private static Optional<PackageId> commonRootPackage(List<PackageId> packageIds) {
        if (packageIds.isEmpty()) return Optional.empty();
        Optional<PackageId> candidate = Optional.of(packageIds.getFirst());
        while (candidate.isPresent()) {
            PackageId current = candidate.get();
            if (packageIds.stream().allMatch(packageId -> isSelfOrSubpackage(packageId, current))) return candidate;
            candidate = current.parentIfExist();
        }
        return Optional.empty();
    }

    private static boolean isSelfOrSubpackage(PackageId packageId, PackageId ancestor) {
        return packageId.equals(ancestor) || packageId.isSubpackageOf(ancestor);
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
                .map(entry -> new TypeInsight(entry.getKey(), entry.getValue()));
    }

    public List<MethodInsight> methodInsightList() {
        return values.stream()
                .sorted(Comparator.comparing(MethodInsight::fqn))
                .toList();
    }
}
