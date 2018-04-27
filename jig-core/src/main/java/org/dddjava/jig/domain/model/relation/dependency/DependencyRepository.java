package org.dddjava.jig.domain.model.relation.dependency;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public interface DependencyRepository {
    void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers);

    TypeDependencies findAllTypeDependency();
}
