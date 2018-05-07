package org.dddjava.jig.domain.model.characteristic;

/**
 * 属するレイヤー
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
