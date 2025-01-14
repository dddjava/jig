package org.dddjava.jig.domain.model.identifier.namespace;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageIdentifiersTest {

    @Test
    void MaxDepth() {
        PackageIdentifiers sut = new PackageIdentifiers(Arrays.asList(
                PackageIdentifier.valueOf("a.b.c.d"),
                PackageIdentifier.valueOf("a"),
                PackageIdentifier.valueOf("a.b.c.d.e"),
                PackageIdentifier.valueOf("a.b.c.d")
        ));
        PackageDepth depth = sut.maxDepth();
        assertEquals(5, depth.value());
    }

    @Test
    void applyDepth1() {
        PackageIdentifiers sut = new PackageIdentifiers(Arrays.asList(
                PackageIdentifier.valueOf("a.b.c.d"),
                PackageIdentifier.valueOf("a"),
                PackageIdentifier.valueOf("a.b.c.d.e"),
                PackageIdentifier.valueOf("a.b.c.d")
        )).applyDepth(new PackageDepth(3));

        assertEquals(3, sut.maxDepth().value());
    }

    @Test
    void applyDepth2() {
        PackageIdentifiers sut = new PackageIdentifiers(Arrays.asList(
                PackageIdentifier.valueOf("a.b.c.d"),
                PackageIdentifier.valueOf("a"),
                PackageIdentifier.valueOf("a.b.c.d.e"),
                PackageIdentifier.valueOf("a.b.c.d")
        )).applyDepth(new PackageDepth(100));

        assertEquals(5, sut.maxDepth().value());
    }
}