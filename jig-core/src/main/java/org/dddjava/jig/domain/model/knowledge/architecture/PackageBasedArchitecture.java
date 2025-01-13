package org.dddjava.jig.domain.model.knowledge.architecture;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 上位パッケージの構成
 */
public class PackageBasedArchitecture {

    List<PackageIdentifier> architecturePackages;

    PackageBasedArchitecture(List<PackageIdentifier> architecturePackages) {
        this.architecturePackages = architecturePackages;
    }

    public static PackageBasedArchitecture from(JigTypes jigTypes) {
        return new PackageBasedArchitecture(getArchitecturePackages(jigTypes));
    }

    private static List<PackageIdentifier> getArchitecturePackages(JigTypes jigTypes) {
        Map<PackageIdentifier, List<JigType>> packageIdentifierListMap = jigTypes.stream()
                .collect(groupingBy(JigType::packageIdentifier));
        // depth単位にリストにする
        Map<Integer, List<PackageIdentifier>> depthMap = packageIdentifierListMap.keySet().stream()
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .sorted(Comparator.comparing(PackageIdentifier::asText))
                .distinct()
                .collect(groupingBy(packageIdentifier -> packageIdentifier.depth().value()));

        // 最初に同じ深さに2件以上入っているものが出てきたらアーキテクチャパッケージとして扱う
        return depthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(Collections.emptyList());
    }

    public PackageIdentifier packageIdentifier(TypeIdentifier arg) {
        TypeIdentifier typeIdentifier = arg.normalize().unarray();
        for (PackageIdentifier architecturePackage : architecturePackages) {
            if (typeIdentifier.fullQualifiedName().startsWith(architecturePackage.asText())) {
                return architecturePackage;
            }
        }

        String fqn = typeIdentifier.fullQualifiedName();
        // 2階層までに丸める
        int depth = 2;
        String[] split = fqn.split("\\.");
        String name = Arrays.stream(split)
                .limit(split.length <= depth ? split.length - 1 : depth)
                .collect(Collectors.joining("."));
        return PackageIdentifier.valueOf(name);
    }

    public List<PackageIdentifier> architecturePackages() {
        return architecturePackages;
    }
}
