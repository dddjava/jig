package org.dddjava.jig.domain.model.jigloaded.relation.packages;

/**
 * 依存関係の数
 */
public class RelationNumber {
    int value;

    public RelationNumber(int value) {
        this.value = value;
    }

    public String asText() {
        return Integer.toString(value);
    }
}
