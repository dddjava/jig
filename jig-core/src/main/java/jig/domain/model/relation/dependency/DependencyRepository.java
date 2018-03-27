package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.PackageIdentifier;

public interface DependencyRepository {

    PackageDependencies all();

    void registerDependency(PackageIdentifier from, PackageIdentifier to);
}
