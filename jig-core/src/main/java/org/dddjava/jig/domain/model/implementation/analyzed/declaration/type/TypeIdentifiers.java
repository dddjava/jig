package org.dddjava.jig.domain.model.implementation.analyzed.declaration.type;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.type.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 型の識別子一覧
 */
public class TypeIdentifiers {

    List<TypeIdentifier> identifiers;

    public TypeIdentifiers(List<TypeIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<TypeIdentifier> list() {
        ArrayList<TypeIdentifier> list = new ArrayList<>(this.identifiers);
        list.sort(Comparator.comparing(TypeIdentifier::fullQualifiedName));
        return list;
    }

    public static Collector<TypeIdentifier, ?, TypeIdentifiers> collector() {
        return Collectors.collectingAndThen(Collectors.toList(), TypeIdentifiers::new);
    }

    public String asSimpleText() {
        return Text.sortedOf(identifiers.stream().distinct().collect(Collectors.toList()), TypeIdentifier::asSimpleText);
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return identifiers.contains(typeIdentifier);
    }

    public boolean empty() {
        return identifiers.isEmpty();
    }

    public PackageIdentifiers packageIdentifiers() {
        List<PackageIdentifier> availablePackages = identifiers.stream()
                .map(TypeIdentifier::packageIdentifier)
                .distinct()
                .collect(Collectors.toList());
        return new PackageIdentifiers(availablePackages);
    }

    public TypeIdentifiers normalize() {
        return identifiers.stream().map(TypeIdentifier::normalize).distinct().collect(collector());
    }
}
