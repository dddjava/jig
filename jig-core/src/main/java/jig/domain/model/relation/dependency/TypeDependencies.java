package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.namespace.PackageIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TypeDependencies {

    List<TypeDependency> list;

    public TypeDependencies(List<TypeDependency> list) {
        this.list = list;
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
}
