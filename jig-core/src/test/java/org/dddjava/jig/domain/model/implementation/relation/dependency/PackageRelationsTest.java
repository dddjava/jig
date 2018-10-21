package org.dddjava.jig.domain.model.implementation.relation.dependency;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.networks.packages.PackageRelation;
import org.dddjava.jig.domain.model.networks.packages.PackageRelations;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PackageRelationsTest {

    @Test
    void test() {
        List<PackageRelation> dependencies = new ArrayList<>();
        dependencies.add(new PackageRelation(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.b")));
        dependencies.add(new PackageRelation(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.c")));
        dependencies.add(new PackageRelation(new PackageIdentifier("x.y.z.a"), new PackageIdentifier("x.y.z.d")));

        PackageRelations sut = new PackageRelations(dependencies);

        assertThat(sut.number().asText()).isEqualTo("3");
    }
}