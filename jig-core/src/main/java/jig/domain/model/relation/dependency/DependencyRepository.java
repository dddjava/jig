package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public TypeDependencies findAllTypeDependency() {
        List<TypeDependency> list = map.entrySet().stream()
                .flatMap(entry -> entry.getValue().list().stream()
                        .map(to -> new TypeDependency(entry.getKey(), to)))
                .collect(Collectors.toList());
        return new TypeDependencies(list);
    }
}
