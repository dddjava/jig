package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.identifier.namespace.PackageIdentifiers;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public PackageDependencies toPackageDependenciesWith(TypeIdentifiers availableTypes) {
        Set<TypeIdentifier> available = availableTypes.set();
        List<PackageDependency> packageDependencyList = list.stream()
                // 両方が引数に含まれるものだけにする
                .filter(typeDependency -> typeDependency.bothMatch(available::contains))
                .map(TypeDependency::toPackageDependency)
                .filter(PackageDependency::notSelfRelation)
                .distinct()
                .collect(Collectors.toList());

        List<PackageIdentifier> availablePackages = available.stream().map(TypeIdentifier::packageIdentifier).collect(Collectors.toList());
        PackageIdentifiers allPackages = new PackageIdentifiers(availablePackages);

        return new PackageDependencies(packageDependencyList, allPackages);
    }

    public TypeDependencyStream stream() {
        return new TypeDependencyStream(list.stream());
    }
}
