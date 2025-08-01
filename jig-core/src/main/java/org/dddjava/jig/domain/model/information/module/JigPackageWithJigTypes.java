package org.dddjava.jig.domain.model.information.module;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * パッケージ単位のJigTypeのグループ
 */
public record JigPackageWithJigTypes(PackageId packageId, List<JigType> jigTypes) {

    public static List<JigPackageWithJigTypes> from(JigTypes jigTypes) {
        Map<PackageId, List<JigType>> map = jigTypes.orderedStream()
                .collect(Collectors.groupingBy(JigType::packageId));
        return map.entrySet().stream()
                .map(entity -> new JigPackageWithJigTypes(entity.getKey(), entity.getValue()))
                .sorted(Comparator.comparing(jigPackageWithJigTypes -> jigPackageWithJigTypes.packageId().asText()))
                .toList();
    }
}
