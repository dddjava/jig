package jig.domain.model.relation.dependency;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.namespace.PackageIdentifiers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageDependenciesTest {

    @Test
    void test() {
        List<PackageDependency> dependencies = new ArrayList<>();
        dependencies.add(new PackageDependency(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.b")));
        dependencies.add(new PackageDependency(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.c")));
        dependencies.add(new PackageDependency(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.d")));

        PackageDependencies sut = new PackageDependencies(
                dependencies,
                new PackageIdentifiers(Collections.emptyList()));

        int size = sut.list().size();
        assertThat(size).isEqualTo(3);
    }
}