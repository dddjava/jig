package org.dddjava.jig.domain.model.jigmodel.architecture;

import org.dddjava.jig.domain.model.parts.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * アーキテクチャ構成要素コレクション
 */
public class ArchitectureComponents {

    List<PackageIdentifier> architecturePackages;

    public ArchitectureComponents(List<PackageIdentifier> architecturePackages) {
        this.architecturePackages = architecturePackages;
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
        return new PackageIdentifier(name);
    }

    public List<PackageIdentifier> architecturePackages() {
        return architecturePackages;
    }
}
