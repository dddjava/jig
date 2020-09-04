package org.dddjava.jig.domain.model.jigdocument.stationery;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageStructureTest {

    @Test
    void test() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        String dotText = packageStructure.toDotText(Node::typeOf);
        assertTrue(dotText.contains("\"a.b.c.X\""));
    }

    @Test
    void test2() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        typeIdentifiers.add(new TypeIdentifier("a.b.c.Y"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        String dotText = packageStructure.toDotText(Node::typeOf);
        assertFalse(dotText.contains("\"cluster_a.b.c\""));
        assertTrue(dotText.contains("\"a.b.c.X\""));
        assertTrue(dotText.contains("\"a.b.c.Y\""));
    }

    @Test
    void test3() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        typeIdentifiers.add(new TypeIdentifier("a.b.c.d.Y"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        String dotText = packageStructure.toDotText(Node::typeOf);
        assertFalse(dotText.contains("\"cluster_a.b.c\""));
        assertTrue(dotText.contains("\"cluster_a.b.c.d\""));
        assertTrue(dotText.contains("\"a.b.c.X\""));
        assertTrue(dotText.contains("\"a.b.c.d.Y\""));
        System.out.println(dotText);
    }

    @Test
    void test4() {
        List<TypeIdentifier> typeIdentifiers = new ArrayList<>();
        typeIdentifiers.add(new TypeIdentifier("a.b.c.X"));
        typeIdentifiers.add(new TypeIdentifier("a.b.d.Y"));
        typeIdentifiers.add(new TypeIdentifier("a.b.d.Z"));
        typeIdentifiers.add(new TypeIdentifier("a.b.d.f.L"));
        PackageStructure packageStructure = PackageStructure.from(typeIdentifiers);

        String dotText = packageStructure.toDotText(Node::typeOf);
        assertFalse(dotText.contains("\"cluster_a.b\""));
        assertTrue(dotText.contains("\"cluster_a.b.c\""));
        assertTrue(dotText.contains("\"cluster_a.b.d\""));
        assertTrue(dotText.contains("\"a.b.c.X\""));
        assertTrue(dotText.contains("\"a.b.d.Y\""));
        assertTrue(dotText.contains("\"a.b.d.Z\""));
        System.out.println(dotText);
    }
}