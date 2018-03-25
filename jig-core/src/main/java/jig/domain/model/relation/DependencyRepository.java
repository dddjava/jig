package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;

public interface DependencyRepository {

    Relations all();

    void registerDependency(Identifier from, Identifier to);

}
