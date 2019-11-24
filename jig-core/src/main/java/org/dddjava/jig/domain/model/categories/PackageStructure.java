package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.diagram.Node;
import org.dddjava.jig.domain.model.diagram.Subgraph;

import java.util.*;
import java.util.function.Function;

public class PackageStructure {

    PackageIdentifier rootPackage;
    Map<PackageIdentifier, Set<PackageIdentifier>> subPackageMap;
    Map<PackageIdentifier, List<TypeIdentifier>> belongTypeMap;

    public PackageStructure(PackageIdentifier rootPackage, Map<PackageIdentifier, Set<PackageIdentifier>> subPackageMap, Map<PackageIdentifier, List<TypeIdentifier>> belongTypeMap) {
        this.rootPackage = rootPackage;
        this.subPackageMap = subPackageMap;
        this.belongTypeMap = belongTypeMap;
    }

    public static PackageStructure from(List<TypeIdentifier> typeIdentifiers) {
        Map<PackageIdentifier, Set<PackageIdentifier>> subPackageMap = new HashMap<>();
        Map<PackageIdentifier, List<TypeIdentifier>> belongTypeMap = new HashMap<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers) {
            PackageIdentifier packageIdentifier = typeIdentifier.packageIdentifier();
            belongTypeMap
                    .computeIfAbsent(packageIdentifier, k -> new ArrayList<>())
                    .add(typeIdentifier);

            PackageIdentifier parent = packageIdentifier.parent();
            while (!parent.equals(packageIdentifier)) {
                subPackageMap
                        .computeIfAbsent(parent, k -> new HashSet<>())
                        .add(packageIdentifier);

                // defaultまで繰り返す
                packageIdentifier = parent;
                parent = packageIdentifier.parent();
            }
        }

        // defaultパッケージからsubpackageが1つの間、子パッケージを探す
        PackageIdentifier rootPackage = PackageIdentifier.defaultPackage();
        Set<PackageIdentifier> packageIdentifiers = subPackageMap.get(PackageIdentifier.defaultPackage());
        while (packageIdentifiers.size() == 1) {
            PackageIdentifier onlyOnePackage = packageIdentifiers.iterator().next();
            rootPackage = onlyOnePackage;
            packageIdentifiers = subPackageMap.get(onlyOnePackage);
            // 所属するクラスがあったら終了
            if (belongTypeMap.containsKey(onlyOnePackage)) {
                break;
            }
        }

        return new PackageStructure(rootPackage, subPackageMap, belongTypeMap);
    }

    public String toDotText(Function<PackageIdentifier, Subgraph> packageWriter, Function<TypeIdentifier, Node> typeWriter) {
        return toDotTextInternal(rootPackage, packageWriter, typeWriter);
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
