package org.dddjava.jig.domain.model.networks;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public interface DependencyRepository {
    void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers);

    TypeDependencies findAllTypeDependency();
}
