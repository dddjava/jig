package jig.domain.model.relation;

import jig.domain.model.identifier.TypeIdentifier;

public interface DependencyRepository {

    Relations all();

    void registerDependency(TypeIdentifier from, TypeIdentifier to);

}
