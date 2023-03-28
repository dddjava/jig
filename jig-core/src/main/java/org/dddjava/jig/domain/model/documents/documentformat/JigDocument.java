package org.dddjava.jig.domain.model.documents.documentformat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    BusinessRuleList(
            JigDocumentLabel.of("ビジネスルール一覧", "BusinessRuleList"),
            "business-rule", JigDocumentType.LIST),

    /**
     * パッケージ関連図
     *
     * ビジネスルールのパッケージ関連を可視化する。
     * トップレベルからの階層(depth)で丸めて複数出力される。
     * パッケージの関連有無や方向からドメインを語れるかのウォークスルーに使用する。
     */
    PackageRelationDiagram(
            JigDocumentLabel.of("パッケージ関連図", "PackageRelationDiagram"),
            "package-relation", JigDocumentType.DIAGRAM),

    /**
     * ビジネスルール関連図
     *
     * ビジネスルール間の関連を可視化する。
     * クラス名と依存線のみのクラス図。ある程度以上の規模になると大きくなりすぎて使いづらくなる。
     * パッケージ関連図で把握できない場合の補助に使用する。
     */
    BusinessRuleRelationDiagram(
            JigDocumentLabel.of("ビジネスルール関連図", "BusinessRuleRelationDiagram"),
            "business-rule-relation", JigDocumentType.DIAGRAM),
    OverconcentrationBusinessRuleDiagram(
            JigDocumentLabel.of("集中ビジネスルールツリー図", "OverconcentrationBusinessRuleDiagram"),
            "business-rule-overconcentration", JigDocumentType.DIAGRAM),
    CoreBusinessRuleRelationDiagram(
            JigDocumentLabel.of("コアビジネスルール関連図", "CoreBusinessRuleRelationDiagram"),
            "business-rule-core", JigDocumentType.DIAGRAM),

    /**
     * 区分図
     *
     * 区分と区分値を可視化する。
     * 区分の充実はドメインの把握具合と密接に関わる。
     */
    CategoryDiagram(
            JigDocumentLabel.of("区分図", "CategoryDiagram"),
            "category", JigDocumentType.DIAGRAM),

    /**
     * 区分使用図
     *
     * 区分を使用しているクラスを可視化する。
     */
    CategoryUsageDiagram(
            JigDocumentLabel.of("区分使用図", "CategoryUsageDiagram"),
            "category-usage", JigDocumentType.DIAGRAM),

    /**
     * 機能一覧
     *
     * 機能を提供するメソッドの一覧。
     * 三層（プレゼンテーション層、アプリケーション層、データソース層）の情報を提供する。
     * アプリケーションの状況把握に使用できる。
     *
     * 制限事項: {@link org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader}
     */
    ApplicationList(
            JigDocumentLabel.of("機能一覧", "ApplicationList"),
            "application", JigDocumentType.LIST),

    /**
     * サービスメソッド呼び出し図
     *
     * サービスクラスのメソッド呼び出しを可視化する。
     */
    ServiceMethodCallHierarchyDiagram(
            JigDocumentLabel.of("サービスメソッド呼び出し図", "ServiceMethodCallHierarchyDiagram"),
            "service-method-call-hierarchy", JigDocumentType.DIAGRAM),

    /**
     * ユースケース複合図
     */
    CompositeUsecaseDiagram(
            JigDocumentLabel.of("ユースケース複合図", "CompositeUsecaseDiagram"),
            "composite-usecase", JigDocumentType.DIAGRAM),

    /**
     * アーキテクチャ図
     */
    ArchitectureDiagram(
            JigDocumentLabel.of("アーキテクチャ図", "ArchitectureDiagram"),
            "architecture", JigDocumentType.DIAGRAM),

    /**
     * コンポーネント関連図
     */
    ComponentRelationDiagram(
            JigDocumentLabel.of("コンポーネント関連図", "ComponentRelationDiagram"),
            "component-relation", JigDocumentType.DIAGRAM),

    /**
     * ドメイン概要
     */
    DomainSummary(
            JigDocumentLabel.of("ドメイン概要", "domain"),
            "domain", JigDocumentType.SUMMARY),

    /**
     * アプリケーション概要
     */
    ApplicationSummary(
            JigDocumentLabel.of("アプリケーション概要", "application"),
            "application", JigDocumentType.SUMMARY),

    /**
     * 区分概要
     */
    EnumSummary(
            JigDocumentLabel.of("区分概要", "enum"),
            "enum", JigDocumentType.SUMMARY),

    /**
     * スキーマ概要
     */
    SchemaSummary(
            JigDocumentLabel.of("スキーマ概要", "domain-schema"),
            "domain-schema", JigDocumentType.SUMMARY),

    /**
     * 用語集
     */
    TermList(
            JigDocumentLabel.of("用語集", "term"),
            "term", JigDocumentType.LIST),
    TermTable(
            JigDocumentLabel.of("用語集", "term"),
            "term", JigDocumentType.TABLE);

    private final JigDocumentLabel label;
    private final String documentFileName;
    private final JigDocumentType jigDocumentType;

    JigDocument(
            JigDocumentLabel label, String documentFileName, JigDocumentType jigDocumentType) {
        this.label = label;
        this.documentFileName = documentFileName;
        this.jigDocumentType = jigDocumentType;
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
                .map(
                        JigDocument::valueOf)
                .collect(Collectors.toList());
    }

    public JigDocumentType jigDocumentType() {
        return jigDocumentType;
    }

    public String label() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage().equals("en") ? label.english : label.japanese;
    }
}
