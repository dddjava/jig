package org.dddjava.jig.presentation.view;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 取り扱うドキュメントの種類
 */
public enum JigDocument {

    /**
     * サービスメソッド呼び出しダイアグラム
     *
     * サービスクラスのメソッド呼び出しを可視化する。
     */
    ServiceMethodCallHierarchyDiagram("service-method-call-hierarchy"),

    /**
     * パッケージ関連ダイアグラム
     *
     * ビジネスルールのパッケージ関連を可視化する。
     * トップレベルからの階層(depth)で丸めて複数出力される。
     * パッケージの関連有無や方向からドメインを語れるかのウォークスルーに使用する。
     */
    PackageRelationDiagram("package-relation"),

    /**
     * ビジネスルール関連ダイアグラム
     *
     * ビジネスルール間の関連を可視化する。
     * クラス名と依存線のみのクラス図。ある程度以上の規模になると大きくなりすぎて使いづらくなる。
     * パッケージ関連ダイアグラムで把握できない場合の補助に使用する。
     */
    BusinessRuleRelationDiagram("business-rule-relation"),

    /**
     * 機能一覧
     *
     * 機能を提供するメソッドの一覧。
     * 三層（プレゼンテーション層、アプリケーション層、データソース層）の情報を提供する。
     * アプリケーションの状況把握に使用できる。
     *
     * 制限事項: {@link org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader}
     */
    ApplicationList("application"),

    /**
     * ビジネスルール一覧
     *
     * ビジネスルールを表すクラスの一覧。
     * 用語集としてや、ビジネスルールの充足具合の把握などに使用できる。
     */
    BusinessRuleList("business-rule"),

    /**
     * 分岐数一覧
     *
     * メソッドごとの分岐数の一覧。
     */
    BranchList("branches"),

    /**
     * 区分使用ダイアグラム
     *
     * 区分を使用しているクラスを可視化する。
     */
    CategoryUsageDiagram("category-usage"),

    /**
     * 区分ダイアグラム
     *
     * 区分と区分値を可視化する。
     * 区分の充実はドメインの把握具合と密接に関わる。
     */
    CategoryDiagram("category"),

    /**
     * 真偽値サービス関連ダイアグラム
     *
     * 真偽値を返すサービスと使用しているメソッドを可視化する。
     * 真偽値にビジネスルールが埋もれていないかの検出に使用できる。
     */
    BooleanServiceDiagram("boolean-service"),

    /**
     * パッケージツリーダイアグラム
     *
     * パッケージ階層を可視化する。
     */
    PackageTreeDiagram("package-tree");

    private final String documentFileName;

    JigDocument(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public static List<JigDocument> canonical() {
        return Arrays.stream(values())
                .filter(value -> value != PackageTreeDiagram)
                .collect(Collectors.toList());
    }

    public String fileName() {
        return documentFileName;
    }

    public static List<JigDocument> resolve(String diagramTypes) {
        return Arrays.stream(diagramTypes.split(","))
                .map(JigDocument::valueOf)
                .collect(Collectors.toList());
    }
}
