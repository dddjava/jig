package org.dddjava.jig.domain.model.jigdocument.documentformat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 取り扱うドキュメントの種類
 */
public enum JigDocument {

    /**
     * ビジネスルール一覧
     *
     * ビジネスルールを表すクラスの一覧。
     * 用語集としてや、ビジネスルールの充足具合の把握などに使用できる。
     */
    BusinessRuleList("business-rule"),

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
    OverconcentrationBusinessRuleDiagram("business-rule-overconcentration"),
    CoreBusinessRuleRelationDiagram("business-rule-core"),

    /**
     * 区分ダイアグラム
     *
     * 区分と区分値を可視化する。
     * 区分の充実はドメインの把握具合と密接に関わる。
     */
    CategoryDiagram("category"),

    /**
     * 区分使用ダイアグラム
     *
     * 区分を使用しているクラスを可視化する。
     */
    CategoryUsageDiagram("category-usage"),

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
     * サービスメソッド呼び出しダイアグラム
     *
     * サービスクラスのメソッド呼び出しを可視化する。
     */
    ServiceMethodCallHierarchyDiagram("service-method-call-hierarchy"),

    /**
     * ユースケース複合図
     */
    CompositeUsecaseDiagram("composite-usecase"),

    /**
     * アーキテクチャダイアグラム
     */
    ArchitectureDiagram("architecture"),
    ;

    private final String documentFileName;

    JigDocument(String documentFileName) {
        this.documentFileName = documentFileName;
    }

    public static List<JigDocument> canonical() {
        return Arrays.stream(values())
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
