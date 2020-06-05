package org.dddjava.jig.domain.model.jigmodel.lowmodel;

/**
 * 型の種類
 */
public enum TypeKind {
    通常型,

    列挙型,
    // 抽象列挙型＝継承される列挙型。継承クラスが生成され、多態である。
    抽象列挙型,
    アノテーション,
    インタフェース,
    抽象型;

    public boolean isCategory() {
        return this == 列挙型 || this == 抽象列挙型;
    }
}
