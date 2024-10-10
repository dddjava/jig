package org.dddjava.jig.domain.model.parts.term;

import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TermIdentifierTest {

    @Test
    void パッケージ() throws Exception {
        Term term = Term.fromPackage(
                PackageIdentifier.valueOf("this.is.package"),
                "title", "description");
        assertEquals("package", term.identifier().simpleText());
    }

    @Test
    void 引数なしメソッド() throws Exception {
        Term term = Term.fromMethod(
                new MethodIdentifier(new TypeIdentifier("this.is.type"), new MethodSignature("method")),
                "title", "description");
        assertEquals("method", term.identifier().simpleText());
    }

    @Test
    void 引数ありメソッド() throws Exception {
        Term term = Term.fromMethod(
                new MethodIdentifier(new TypeIdentifier("this.is.type"), new MethodSignature("method", TypeIdentifier.of(String.class))),
                "title", "description");
        assertEquals("method", term.identifier().simpleText(), () -> term.identifier().asText());
    }
}