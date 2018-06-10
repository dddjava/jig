package org.dddjava.jig.domain.model.implementation.relation.dependency;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.networks.packages.PackageDependencies;
import org.dddjava.jig.domain.model.networks.packages.PackageDependency;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageDependenciesTest {

    @Test
    void test() {
        List<PackageDependency> dependencies = new ArrayList<>();
        dependencies.add(new PackageDependency(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.b")));
        dependencies.add(new PackageDependency(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.c")));
        dependencies.add(new PackageDependency(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.d")));

        PackageDependencies sut = new PackageDependencies(dependencies);

        assertThat(sut.number().asText()).isEqualTo("3");
    }
}