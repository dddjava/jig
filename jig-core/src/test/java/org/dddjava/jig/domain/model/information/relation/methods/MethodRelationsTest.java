package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
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
                JigMethodIdentifier.from(TypeIdentifier.valueOf("dummy"), from, List.of()),
                JigMethodIdentifier.from(TypeIdentifier.valueOf("dummy"), to, List.of()));
    }
}