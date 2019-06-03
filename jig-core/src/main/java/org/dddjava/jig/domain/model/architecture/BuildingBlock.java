package org.dddjava.jig.domain.model.architecture;

/**
 * アーキテクチャの構成要素
 */
public enum BuildingBlock {
    PRESENTATION,
    APPLICATION,
    DATASOURCE,
    BUSINESS_RULE,
    OTHER;

    public String asText() {
        return name();
    }
}
