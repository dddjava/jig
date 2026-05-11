package org.dddjava.jig.domain.model.sources.javasources;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * 型・パッケージとJavaソースファイルパスの対応。
 * JigType本体には保持せず、別経路で参照するために用いる。
 */
public record TypeSourcePaths(Map<TypeId, Path> typeMap, Map<PackageId, Path> packageMap) {

    public static TypeSourcePaths empty() {
        return new TypeSourcePaths(Map.of(), Map.of());
    }

    public Optional<Path> find(TypeId typeId) {
        return Optional.ofNullable(typeMap.get(typeId));
    }

    public Optional<Path> find(PackageId packageId) {
        return Optional.ofNullable(packageMap.get(packageId));
    }
}
