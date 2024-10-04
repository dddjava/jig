package org.dddjava.jig.domain.model.parts.classes.rdbaccess;

public enum SqlReadStatus {
    成功,
    SQLなし,
    読み取り失敗あり,
    失敗,
    未処理;

    public boolean not正常() {
        return this != 成功;
    }
}
