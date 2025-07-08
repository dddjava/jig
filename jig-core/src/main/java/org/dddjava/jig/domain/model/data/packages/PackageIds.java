package org.dddjava.jig.domain.model.data.packages;

import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * パッケージ識別子一覧
 */
public record PackageIds(Set<PackageId> values) {

    public PackageIds applyDepth(PackageDepth packageDepth) {
        Set<PackageId> set = values.stream()
                .map(packageId -> packageId.applyDepth(packageDepth))
                .collect(Collectors.toSet());
        return new PackageIds(set);
    }

    public PackageDepth maxDepth() {
        return values.stream()
                .map(PackageId::depth)
                .max(Comparator.comparing(PackageDepth::value))
                .orElseGet(() -> new PackageDepth(0));
    }

    public String countDescriptionText() {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return (isEnglish ? "Packages: " : "パッケージ数: ") + values.size();
    }
}
