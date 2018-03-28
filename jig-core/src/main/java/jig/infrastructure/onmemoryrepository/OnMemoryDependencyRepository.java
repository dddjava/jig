package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.relation.dependency.DependencyRepository;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Repository
public class OnMemoryDependencyRepository implements DependencyRepository {

    final Set<PackageDependency> set;

    public OnMemoryDependencyRepository() {
        this.set = new HashSet<>();
    }

    @Override
    public void registerDependency(PackageIdentifier from, PackageIdentifier to) {
        set.add(new PackageDependency(from, to));
    }

    @Override
    public PackageDependencies all() {
        return new PackageDependencies(new ArrayList<>(set));
    }
}
