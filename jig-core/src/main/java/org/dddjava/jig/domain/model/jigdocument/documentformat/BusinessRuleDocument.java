package org.dddjava.jig.domain.model.jigdocument.documentformat;

/**
 * ビジネスルールドキュメント
 */
public enum BusinessRuleDocument {

    /**
     * ビジネスルール一覧
     *
     * ビジネスルールを表すクラスの一覧。
     * 用語集としてや、ビジネスルールの充足具合の把握などに使用できる。
     */
    BusinessRuleList,

    /**
     * パッケージ関連ダイアグラム
     *
     * ビジネスルールのパッケージ関連を可視化する。
     * トップレベルからの階層(depth)で丸めて複数出力される。
     * パッケージの関連有無や方向からドメインを語れるかのウォークスルーに使用する。
     */
    PackageRelationDiagram,

    /**
     * ビジネスルール関連ダイアグラム
     *
     * ビジネスルール間の関連を可視化する。
     * クラス名と依存線のみのクラス図。ある程度以上の規模になると大きくなりすぎて使いづらくなる。
     * パッケージ関連ダイアグラムで把握できない場合の補助に使用する。
     */
    BusinessRuleRelationDiagram,

    /**
     * 区分ダイアグラム
     *
     * 区分と区分値を可視化する。
     * 区分の充実はドメインの把握具合と密接に関わる。
     */
    CategoryDiagram,

    /**
     * 区分使用ダイアグラム
     *
     * 区分を使用しているクラスを可視化する。
     */
    CategoryUsageDiagram;
}
