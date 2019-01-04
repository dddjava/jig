package org.dddjava.jig.domain.model.implementation.analyzed.architecture;

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
