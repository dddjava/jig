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
     * パッケージ依存ダイアグラム
     *
     * パッケージ間の依存を可視化する。
     * トップレベルからの階層(depth)で丸めて複数出力する。
     * 出力対象パッケージはモデルと判断されるもの。
     */
    PackageRelationDiagram("package-relation"),

    /**
     * ビジネスルール関連ダイアグラム
     *
     * ビジネスルール間の関連を可視化する。
     */
    BusinessRuleRelationDiagram("business-rule-relation"),

    /**
     * 機能一覧
     *
     * 機能を提供するメソッドの一覧。
     *
     * 制限事項: {@link org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader}
     */
    ApplicationList("application"),

    /**
     * ビジネスルール一覧
     *
     * ビジネスルールを表すクラスの一覧。
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
     * enumの使用クラスを可視化する。
     */
    CategoryUsageDiagram("category-usage"),

    /**
     * 区分ダイアグラム
     *
     * enumと値の1枚絵
     */
    CategoryDiagram("category"),

    /**
     * 真偽値サービス関連ダイアグラム
     *
     * booleanを返すサービスと使用しているメソッドを可視化する。
     */
    BooleanServiceDiagram("boolean-service");

    private final String documentFileName;

    JigDocument(String documentFileName) {
        this.documentFileName = documentFileName;
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
