package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;

public class PackageStructure {

    PackageIdentifier rootPackage;
    Map<PackageIdentifier, Set<PackageIdentifier>> subPackageMap;
    Map<PackageIdentifier, List<TypeIdentifier>> belongTypeMap;

    PackageStructure(PackageIdentifier rootPackage, Map<PackageIdentifier, Set<PackageIdentifier>> subPackageMap, Map<PackageIdentifier, List<TypeIdentifier>> belongTypeMap) {
        this.rootPackage = rootPackage;
        this.subPackageMap = subPackageMap;
        this.belongTypeMap = belongTypeMap;
    }

    public static PackageStructure from(List<TypeIdentifier> typeIdentifiers) {

        Map<PackageIdentifier, List<TypeIdentifier>> belongTypeMap = typeIdentifiers.stream()
                .collect(groupingBy(TypeIdentifier::packageIdentifier));

        Map<PackageIdentifier, Set<PackageIdentifier>> packageMap = new HashMap<>();
        for (PackageIdentifier packageIdentifier : belongTypeMap.keySet()) {
            PackageIdentifier current = packageIdentifier;
            while (current.hasName()) {
                PackageIdentifier parent = current.parent();
                packageMap.computeIfAbsent(parent, key -> new HashSet<>()).add(current);
                current = parent;
            }
        }
        PackageIdentifier rootPackage = PackageIdentifier.defaultPackage();
        while (!belongTypeMap.containsKey(rootPackage)
                && packageMap.containsKey(rootPackage)
                && packageMap.get(rootPackage).size() == 1) {
            PackageIdentifier nextRootPackage = packageMap.get(rootPackage).iterator().next();
            packageMap.remove(rootPackage);
            rootPackage = nextRootPackage;
        }

        return new PackageStructure(rootPackage, packageMap, belongTypeMap);
    }

    public String toDotText(Function<TypeIdentifier, Node> typeWriter) {
        return toDotTextInternal(
                rootPackage,
                packageIdentifier -> new Subgraph(packageIdentifier.asText())
                        .label(packageIdentifier.simpleName()),
                typeWriter);
    }

    private String toDotTextInternal(PackageIdentifier basePackage, Function<PackageIdentifier, Subgraph> packageWriter, Function<TypeIdentifier, Node> typeWriter) {
        StringJoiner stringJoiner = new StringJoiner("\n");
        for (PackageIdentifier packageIdentifier : subPackageMap.getOrDefault(basePackage, Collections.emptySet())) {
            Subgraph subgraph = packageWriter.apply(packageIdentifier);
            StringJoiner types = new StringJoiner("\n");
            for (TypeIdentifier typeIdentifier : belongTypeMap.getOrDefault(packageIdentifier, Collections.emptyList())) {
                types.add(typeWriter.apply(typeIdentifier).asText());
            }

            subgraph.color("lightgray")
                    .add(toDotTextInternal(packageIdentifier, packageWriter, typeWriter))
                    .add(types.toString());
            stringJoiner.add(subgraph.toString());
        }
        return stringJoiner.toString();
    }
}
