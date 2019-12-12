package org.dddjava.jig.domain.model.jigsource.datasource;

public enum SqlReadStatus {
    成功,
    SQLなし,
    読み取り失敗あり,
    失敗;

    public boolean not正常() {
        return this != 成功;
    }
}
