package org.dddjava.jig.domain.model.identifier.namespace;

import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageIdentifiers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PackageIdentifiersTest {

    @Test
    void MaxDepth() {
        PackageIdentifiers sut = new PackageIdentifiers(Arrays.asList(
                new PackageIdentifier("a.b.c.d"),
                new PackageIdentifier("a"),
                new PackageIdentifier("a.b.c.d.e"),
                new PackageIdentifier("a.b.c.d")
        ));
        PackageDepth depth = sut.maxDepth();
        assertThat(depth.value()).isEqualTo(5);
    }

    @Test
    void applyDepth1() {
        PackageIdentifiers sut = new PackageIdentifiers(Arrays.asList(
                new PackageIdentifier("a.b.c.d"),
                new PackageIdentifier("a"),
                new PackageIdentifier("a.b.c.d.e"),
                new PackageIdentifier("a.b.c.d")
        )).applyDepth(new PackageDepth(3));

        assertThat(sut.maxDepth().value()).isEqualTo(3);
    }

    @Test
    void applyDepth2() {
        PackageIdentifiers sut = new PackageIdentifiers(Arrays.asList(
                new PackageIdentifier("a.b.c.d"),
                new PackageIdentifier("a"),
                new PackageIdentifier("a.b.c.d.e"),
                new PackageIdentifier("a.b.c.d")
        )).applyDepth(new PackageDepth(100));

        assertThat(sut.maxDepth().value()).isEqualTo(5);
    }
}