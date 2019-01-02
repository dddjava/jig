package org.dddjava.jig.domain.model.implementation.analyzed.datasource;

public enum SqlReadStatus {
    成功,
    SQLなし,
    読み取り失敗あり,
    失敗;

    public boolean not正常() {
        return this != 成功;
    }
}
