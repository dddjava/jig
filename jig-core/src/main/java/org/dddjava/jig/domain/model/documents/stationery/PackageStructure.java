package org.dddjava.jig.domain.model.documents.stationery;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;

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

    private String toDotTextInternal(PackageIdentifier basePackage, Function<PackageIdentifier, Subgraph> packageToSubgraph, Function<TypeIdentifier, Node> typeToNode) {
        StringJoiner stringJoiner = new StringJoiner("\n")
                .add(typesDotText(basePackage, typeToNode));
        for (PackageIdentifier packageIdentifier : subPackageMap.getOrDefault(basePackage, Collections.emptySet())) {
            Subgraph subgraph = packageToSubgraph.apply(packageIdentifier)
                    .color("lightgray")
                    .add(toDotTextInternal(packageIdentifier, packageToSubgraph, typeToNode));
            stringJoiner.add("").add(subgraph.toString());
        }
        return stringJoiner.toString();
    }

    private String typesDotText(PackageIdentifier packageIdentifier, Function<TypeIdentifier, Node> typeToNode) {
        StringJoiner types = new StringJoiner("\n");
        for (TypeIdentifier typeIdentifier : belongTypeMap.getOrDefault(packageIdentifier, Collections.emptyList())) {
            types.add(typeToNode.apply(typeIdentifier).asText());
        }
        String typesText = types.toString();
        return typesText;
    }
}
