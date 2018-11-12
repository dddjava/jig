package org.dddjava.jig.domain.model.architecture;

/**
 * 属するレイヤー
 */
public enum Layer {
    PRESENTATION,
    APPLICATION,
    DATASOURCE,
    BUSINESS_RULE,
    OTHER;

    public String asText() {
        return name();
    }
}
