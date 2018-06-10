package org.dddjava.jig.domain.model.networks.packages;

/**
 * 依存関係の数
 */
public class DependencyNumber {
    int value;

    public DependencyNumber(int value) {
        this.value = value;
    }

    public String asText() {
        return Integer.toString(value);
    }
}
