package org.dddjava.jig.infrastructure.view.report;

import java.util.Locale;

/**
 * 一覧出力項目
 *
 * 出力項目の名前と並び順の定義。
 */
public enum ReportItem {
    パッケージ名("Package name"),
    パッケージ別名("Package alias"),

    クラス名("Class"),
    メソッドシグネチャ("Method Signature"),
    メソッド戻り値の型("Return class"),

    イベントハンドラ("Event handler"),

    クラス別名("Class alias"),
    メソッド別名("Method alias"),
    メソッド戻り値の型の別名("Return class alias"),
    メソッド引数の型の別名("Arguments alias"),

    使用しているフィールドの型("Using field classes"),

    フィールドの型("Field class"),

    使用箇所数("Number of usage"),
    使用箇所("Usage Classes"),

    クラス数("Number of classes"),
    メソッド数("Number of methods"),
    メソッド一覧("Methods"),

    分岐数("Number of divisions"),

    単純クラス名("Simple class name"),

    // なるべく使わない
    汎用文字列("string"),
    汎用真偽値("boolean"),
    汎用数値("number");

    private final String key;

    ReportItem(String key) {
        this.key = key;
    }

    public String localizedText() {
        Locale locale = Locale.getDefault();
        boolean isEnglish = locale.getLanguage().equals("en");
        return isEnglish ? key : name();
    }
}
