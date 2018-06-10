package org.dddjava.jig.domain.model.networks.type;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;
import org.dddjava.jig.domain.model.networks.packages.PackageDependencies;
import org.dddjava.jig.domain.model.networks.packages.PackageDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 型依存関係一覧
 */
public class TypeDependencies {

    List<TypeDependency> list;

    public TypeDependencies(List<TypeDependency> list) {
        this.list = list;
    }

    public TypeDependencies(ByteCodes byteCodes) {
        this(new ArrayList<>());

        for (ByteCode byteCode : byteCodes.list()) {
            TypeIdentifier form = byteCode.typeIdentifier();
            for (TypeIdentifier to : byteCode.useTypes().list()) {
                list.add(new TypeDependency(form, to));
            }
        }
    }

    public TypeDependencyStream stream() {
        return new TypeDependencyStream(list.stream());
    }

    public PackageDependencies packageDependencies() {
        List<PackageDependency> packageDependencyList = list.stream()
                .map(TypeDependency::toPackageDependency)
                .filter(PackageDependency::notSelfRelation)
                .distinct()
                .collect(Collectors.toList());

        return new PackageDependencies(packageDependencyList);
    }
}
