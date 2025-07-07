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
public record TypeIdentifiers(Set<TypeId> identifiers) {

    public List<TypeId> list() {
        ArrayList<TypeId> list = new ArrayList<>(this.identifiers);
        list.sort(Comparator.comparing(TypeId::fullQualifiedName));
        return list;
    }

    public static Collector<TypeId, ?, TypeIdentifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toSet(), TypeIdentifiers::new);
    }

    public String asSimpleText() {
        return identifiers.stream().distinct().toList().stream()
                .map(TypeId::asSimpleText)
                .sorted()
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public boolean contains(TypeId typeId) {
        return identifiers.contains(typeId);
    }

    public boolean empty() {
        return identifiers.isEmpty();
    }

    public PackageIds packageIdentifiers() {
        Set<PackageId> availablePackages = identifiers.stream()
                .map(TypeId::packageIdentifier)
                .collect(Collectors.toSet());
        return new PackageIds(availablePackages);
    }

    public TypeIdentifiers normalize() {
        return identifiers.stream().map(TypeId::normalize).distinct().collect(collector());
    }

    public int size() {
        return identifiers.size();
    }
}
