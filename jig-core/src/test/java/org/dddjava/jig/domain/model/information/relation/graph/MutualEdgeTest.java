package org.dddjava.jig.domain.model.information.relation.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MutualEdgeTest {

    @Test
    void Edgeから生成できる() {
        var actual = MutualEdge.from(new Edge<>("A", "B"));

        assertEquals("A", actual.a());
        assertEquals("B", actual.b());
    }

    @Test
    void Edgeから生成できる_順番が入れ替わる() {
        var actual = MutualEdge.from(new Edge<>("B", "A"));

        assertEquals("A", actual.a());
        assertEquals("B", actual.b());
    }


    @Test
    void 逆順だと生成できない() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MutualEdge<>("B", "A");
        });
    }

    @Test
    void 自己参照は生成できる() {
        assertDoesNotThrow(() -> {
            new MutualEdge<>("A", "A");
        });
    }
}