package org.dddjava.jig.domain.model.information.module;

import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * パッケージ単位のJigTypeのグループ
 */
public record JigTypesPackage(PackageIdentifier packageIdentifier, List<JigType> jigTypes) {

    public static List<JigTypesPackage> from(JigTypes jigTypes) {
        Map<PackageIdentifier, List<JigType>> map = jigTypes.stream()
                .collect(Collectors.groupingBy(JigType::packageIdentifier));
        return map.entrySet().stream()
                .map(entity -> new JigTypesPackage(entity.getKey(), entity.getValue()))
                .sorted(Comparator.comparing(jigTypesPackage -> jigTypesPackage.packageIdentifier().asText()))
                .collect(toList());
    }
}
