package org.dddjava.jig.domain.model.data.classes.type;

/**
 * 型の種類
 */
public enum TypeKind {
    通常型,

    // finalな型
    列挙型,
    // abstractな型
    抽象列挙型,

    // アノテーション、インタフェース、抽象型の判定がAsmClassVisitorで行われていないのでまだ使えない
    //アノテーション,
    //インタフェース,
    //抽象型

    レコード型;

    public boolean isCategory() {
        return this == 列挙型 || this == 抽象列挙型;
    }
}
