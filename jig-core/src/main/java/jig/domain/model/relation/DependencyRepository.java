package jig.domain.model.relation;

import jig.domain.model.identifier.PackageIdentifier;

public interface DependencyRepository {

    Relations all();

    void registerDependency(PackageIdentifier from, PackageIdentifier to);

}
