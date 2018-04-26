package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.dependency.DependencyRepository;
import jig.domain.model.relation.dependency.TypeDependencies;
import jig.domain.model.relation.dependency.TypeDependency;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class OnMemoryDependencyRepository implements DependencyRepository {

    Map<TypeIdentifier, TypeIdentifiers> map = new HashMap<>();

    @Override
    public void registerDependency(TypeIdentifier typeIdentifier, TypeIdentifiers typeIdentifiers) {
        map.put(typeIdentifier, typeIdentifiers);
    }

    @Override
    public TypeDependencies findAllTypeDependency() {
        List<TypeDependency> list = map.entrySet().stream()
                .flatMap(entry -> entry.getValue().list().stream()
                        .map(to -> new TypeDependency(entry.getKey(), to)))
                .collect(Collectors.toList());
        return new TypeDependencies(list);
    }
}
