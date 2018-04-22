package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

// FIXME
@Repository
public class DependencyRepository {

    Map<TypeIdentifier, TypeIdentifiers> map = new HashMap<>();

    public void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers) {
        map.put(typeIdentifier, typeIdentifiers);
    }

    public TypeIdentifiers findDependency(TypeIdentifier identifier) {
        return map.get(identifier);
    }
}
