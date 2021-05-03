package org.dddjava.jig.domain.model.models.architectures;

import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * アーキテクチャ構成要素コレクション
 *
 * TODO PackageBasedArchitectureなどに変更する
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
