package org.dddjava.jig.domain.model.architecture;

/**
 * アーキテクチャの構成要素
 */
public enum ArchitectureBlock {
    PRESENTATION,
    APPLICATION,
    DATASOURCE,
    BUSINESS_RULE,
    OTHER;

    public String asText() {
        return name();
    }
}
