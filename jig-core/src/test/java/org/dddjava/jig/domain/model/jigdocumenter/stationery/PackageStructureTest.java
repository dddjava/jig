package org.dddjava.jig.domain.model.jigdocumenter.stationery;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageStructureTest {

    @Test
    void test() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        assertEquals("a.b.c", packageStructure.rootPackage.toString());
    }

    @Test
    void test2() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        typeIdentifiers.add(new TypeIdentifier("a.b.c.Y"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        assertEquals("a.b.c", packageStructure.rootPackage.toString());
    }

    @Test
    void test3() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        typeIdentifiers.add(new TypeIdentifier("a.b.c.d.Y"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        assertEquals("a.b.c", packageStructure.rootPackage.toString());
    }

    @Test
    void test4() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        typeIdentifiers.add(new TypeIdentifier("a.b.d.Y"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        assertEquals("a.b", packageStructure.rootPackage.toString());
    }
}