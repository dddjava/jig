package org.dddjava.jig.domain.model.jigobject.term;

import org.dddjava.jig.domain.model.data.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.data.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.term.Term;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
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
        MethodIdentifier method = new MethodIdentifier(TypeIdentifier.valueOf("this.is.type"), new MethodSignature("method"));
        Term term = Term.fromMethod(method.asText(), "title", "description");
        assertEquals("method", term.identifier().simpleText());
    }

    @Test
    void 引数ありメソッド() throws Exception {
        MethodIdentifier method = new MethodIdentifier(TypeIdentifier.valueOf("this.is.type"), new MethodSignature("method", TypeIdentifier.from(String.class)));
        Term term = Term.fromMethod(method.asText(), "title", "description");
        assertEquals("method", term.identifier().simpleText(), () -> term.identifier().asText());
    }
}