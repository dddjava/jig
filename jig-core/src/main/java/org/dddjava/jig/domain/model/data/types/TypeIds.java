package org.dddjava.jig.domain.model.data.types;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.packages.PackageIds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 型の識別子一覧
 */
public record TypeIds(Set<TypeId> values) {

    public List<TypeId> list() {
        ArrayList<TypeId> list = new ArrayList<>(this.values);
        list.sort(Comparator.comparing(TypeId::fullQualifiedName));
        return list;
    }

    public static Collector<TypeId, ?, TypeIds> collector() {
        return Collectors.collectingAndThen(Collectors.toSet(), TypeIds::new);
    }

    public String asSimpleText() {
        return values.stream().distinct().toList().stream()
                .map(TypeId::asSimpleText)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public boolean contains(TypeId typeId) {
        return values.contains(typeId);
    }

    public boolean empty() {
        return values.isEmpty();
    }

    public PackageIds packageIdentifiers() {
        Set<PackageId> availablePackages = values.stream()
                .map(TypeId::packageId)
                .collect(Collectors.toSet());
        return new PackageIds(availablePackages);
    }

    public TypeIds normalize() {
        return values.stream().map(TypeId::normalize).distinct().collect(collector());
    }

    public int size() {
        return values.size();
    }
}
