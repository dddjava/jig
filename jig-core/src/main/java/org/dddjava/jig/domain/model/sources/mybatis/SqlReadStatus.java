package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.sources.ReadStatus;

import java.util.Optional;

public enum SqlReadStatus {
    成功,
    SQLなし,
    読み取り失敗あり,
    失敗,
    未処理;

    /**
     * 記録すべき読み取り結果。成功時は記録するものがないため空。
     */
    public Optional<ReadStatus> toReadStatus() {
        return switch (this) {
            case 成功 -> Optional.empty();
            case SQLなし -> Optional.of(ReadStatus.SQLなし);
            case 読み取り失敗あり -> Optional.of(ReadStatus.SQL読み込み一部失敗);
            case 失敗 -> Optional.of(ReadStatus.SQL読み込み失敗);
            case 未処理 -> throw new IllegalStateException(toString());
        };
    }
}
