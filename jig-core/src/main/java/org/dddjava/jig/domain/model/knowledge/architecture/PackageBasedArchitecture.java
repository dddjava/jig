package org.dddjava.jig.domain.model.knowledge.architecture;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * 上位パッケージの構成
 */
public class PackageBasedArchitecture {

    List<PackageId> architecturePackages;
    private final TypeRelationships typeRelationships;

    PackageBasedArchitecture(List<PackageId> architecturePackages, TypeRelationships typeRelationships) {
        this.architecturePackages = architecturePackages;
        this.typeRelationships = typeRelationships;
    }

    public TypeRelationships classRelations() {
        return typeRelationships;
    }

    public static PackageBasedArchitecture from(JigTypes jigTypes) {
        return new PackageBasedArchitecture(getArchitecturePackages(jigTypes), TypeRelationships.from(jigTypes));
    }

    private static List<PackageId> getArchitecturePackages(JigTypes jigTypes) {
        Map<PackageId, List<JigType>> packageIdTypeListMap = jigTypes.orderedStream()
                .collect(groupingBy(JigType::packageId));
        // depth単位にリストにする
        Map<Integer, List<PackageId>> depthMap = packageIdTypeListMap.keySet().stream()
                .flatMap(packageId -> packageId.genealogical().stream())
                .sorted(Comparator.comparing(PackageId::asText))
                .distinct()
                .collect(groupingBy(packageId -> packageId.depth().value()));

        // 最初に同じ深さに2件以上入っているものが出てきたらアーキテクチャパッケージとして扱う
        return depthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(Collections.emptyList());
    }

    public PackageId packageId(TypeId arg) {
        TypeId typeId = arg.normalize().unarray();
        for (PackageId architecturePackage : architecturePackages) {
            if (typeId.fullQualifiedName().startsWith(architecturePackage.asText())) {
                return architecturePackage;
            }
        }

        String fqn = typeId.fullQualifiedName();
        // 2階層までに丸める
        int depth = 2;
        String[] split = fqn.split("\\.");
        String name = Arrays.stream(split)
                .limit(split.length <= depth ? split.length - 1 : depth)
                .collect(Collectors.joining("."));
        return PackageId.valueOf(name);
    }

    public List<PackageId> architecturePackages() {
        return architecturePackages;
    }
}
