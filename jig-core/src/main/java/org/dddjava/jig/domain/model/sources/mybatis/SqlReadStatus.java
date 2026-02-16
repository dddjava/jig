package org.dddjava.jig.domain.model.sources.mybatis;

import org.dddjava.jig.domain.model.sources.ReadStatus;

public enum SqlReadStatus {
    成功,
    SQLなし,
    読み取り失敗あり,
    失敗,
    未処理;

    public ReadStatus toReadStatus() {
        return switch (this) {
            case SQLなし -> ReadStatus.SQLなし;
            case 読み取り失敗あり -> ReadStatus.SQL読み込み一部失敗;
            case 失敗 -> ReadStatus.SQL読み込み失敗;
            default -> throw new IllegalArgumentException(toString());
        };
    }

    public boolean not正常() {
        return this != 成功;
    }
}
