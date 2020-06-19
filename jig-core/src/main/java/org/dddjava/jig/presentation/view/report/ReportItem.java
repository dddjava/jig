package org.dddjava.jig.presentation.view.report;

/**
 * 一覧出力項目
 *
 * 出力項目の名前と並び順の定義。
 */
public enum ReportItem {
    パッケージ名("packageName"),
    パッケージ別名("packageAlias"),

    クラス名("typeName"),
    メソッドシグネチャ("methodSignature"),
    メソッド戻り値の型("returnType"),

    イベントハンドラ("eventHandler"),

    クラス別名("typeAlias"),
    メソッド別名("methodAlias"),
    メソッド戻り値の型の別名("returnTypeAlias"),
    メソッド引数の型の別名("argumentAlias"),

    使用しているフィールドの型("usingFieldType"),

    フィールドの型("fieldType"),

    使用箇所数("userNumber"),
    使用箇所("userType"),

    クラス数("classNumber"),
    メソッド数("methodNumber"),
    メソッド一覧("methods"),

    分岐数("decisionNumber"),

    単純クラス名("simpleTypeName"),

    // なるべく使わない
    汎用文字列("string"),
    汎用真偽値("boolean"),
    汎用数値("number");

    public final String key;

    ReportItem(String key) {
        this.key = key;
    }
}
