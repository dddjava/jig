package org.dddjava.jig.domain.model.information.module;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * パッケージ単位のJigTypeのグループ
 */
public record JigPackageWithJigTypes(PackageIdentifier packageIdentifier, List<JigType> jigTypes) {

    public static List<JigPackageWithJigTypes> from(JigTypes jigTypes) {
        Map<PackageIdentifier, List<JigType>> map = jigTypes.orderedStream()
                .collect(Collectors.groupingBy(JigType::packageIdentifier));
        return map.entrySet().stream()
                .map(entity -> new JigPackageWithJigTypes(entity.getKey(), entity.getValue()))
                .sorted(Comparator.comparing(jigPackageWithJigTypes -> jigPackageWithJigTypes.packageIdentifier().asText()))
                .collect(toList());
    }
}
