package org.dddjava.jig.domain.model.jigmodel.architecture;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * アーキテクチャ構成要素コレクション
 */
public class ArchitectureComponents {

    Map<ArchitectureComponent, Set<TypeIdentifier>> map;

    public ArchitectureComponents(Map<ArchitectureComponent, Set<TypeIdentifier>> map) {
        map.remove(ArchitectureComponent.OTHERS);
        this.map = map;
    }

    public PackageIdentifier packageIdentifier(TypeIdentifier arg) {
        TypeIdentifier typeIdentifier = arg.normalize().unarray();
        for (Map.Entry<ArchitectureComponent, Set<TypeIdentifier>> entry : map.entrySet()) {
            // Architectureとして識別されているものはそのArchitectureComponentとして扱う
            if (entry.getValue().contains(typeIdentifier)) {
                return new PackageIdentifier(entry.getKey().toString());
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
}
