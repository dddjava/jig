package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelation;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodRelationsTest {

    @Test
    void 通常() {
        var source = new MethodRelations(List.of(
                relationOf("A", "B"),
                relationOf("A", "C"),
                relationOf("B", "C"),
                // 同じ関連もそのまま残す
                relationOf("A", "B")
        ));

        assertEquals(4, source.list().size());
    }

    @Test
    void lambdaが展開される() {
        var source = new MethodRelations(List.of(
                relationOf("A", "lambda$a"),
                relationOf("lambda$a", "B")
        ))
                .inlineLambda();

        assertEquals(1, source.list().size());
    }

    @Test
    void name() {
        var source = new MethodRelations(List.of(
                relationOf("A", "lambda$1"),
                relationOf("A", "lambda$2"),
                relationOf("lambda$1", "lambda$1-1"),
                relationOf("lambda$1", "lambda$1-2"),
                relationOf("lambda$1-1", "Z"),
                relationOf("lambda$1-1", "lambda$1-1-1"),
                relationOf("lambda$1-1-1", "Y"),
                relationOf("lambda$1-2", "X"),
                relationOf("lambda$2", "C")
        ));

        var actual = source.inlineLambda();

        assertEquals(4, actual.list().size());
    }

    private static MethodRelation relationOf(String from, String to) {
        return new MethodRelation(
                new MethodDeclaration(
                        TypeIdentifier.valueOf("dummy"),
                        new MethodSignature(from),
                        MethodReturn.fromTypeOnly(TypeIdentifier.valueOf("void"))),
                new MethodDeclaration(
                        TypeIdentifier.valueOf("dummy"),
                        new MethodSignature(to),
                        MethodReturn.fromTypeOnly(TypeIdentifier.valueOf("void")))
        );
    }
}