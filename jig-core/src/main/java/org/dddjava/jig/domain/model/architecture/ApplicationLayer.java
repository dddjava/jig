package org.dddjava.jig.domain.model.architecture;

/**
 * 機能のレイヤー。
 * 三層＋ドメインモデルの三層部分を表す。
 */
public enum ApplicationLayer {
    PRESENTATION(ArchitectureBlock.APPLICATION),
    APPLICATION(ArchitectureBlock.APPLICATION),
    INFRASTRUCTURE(ArchitectureBlock.DATASOURCE);

    ArchitectureBlock architectureBlock;

    ApplicationLayer(ArchitectureBlock architectureBlock) {
        this.architectureBlock = architectureBlock;
    }
}
