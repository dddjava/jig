package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.relation.DependencyRepository;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.Relations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Repository
public class OnMemoryDependencyRepository implements DependencyRepository {

    final Set<Relation> set;

    public OnMemoryDependencyRepository() {
        this.set = new HashSet<>();
    }

    @Override
    public void registerDependency(PackageIdentifier from, PackageIdentifier to) {
        set.add(new Relation(from, to));
    }

    @Override
    public Relations all() {
        return new Relations(new ArrayList<>(set));
    }
}
