package org.dddjava.jig.domain.model.characteristic;

/**
 * 三層のいずれに属するかを表します。
 */
public enum Layer {
    PRESENTATION,
    APPLICATION,
    DATASOURCE,
    OTHER;

    public String asText() {
        return name();
    }
}
