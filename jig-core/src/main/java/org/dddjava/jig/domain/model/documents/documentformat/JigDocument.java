package org.dddjava.jig.domain.model.documents.documentformat;

import org.dddjava.jig.infrastructure.mybatis.MyBatisMyBatisStatementsReader;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
            "business-rule"),

    /**
     * パッケージ関連図
     *
     * ビジネスルールのパッケージ関連を可視化する。
     * トップレベルからの階層(depth)で丸めて複数出力される。
     * パッケージの関連有無や方向からドメインを語れるかのウォークスルーに使用する。
     */
    PackageRelationDiagram(
            JigDocumentLabel.of("パッケージ関連図", "PackageRelationDiagram"),
            "package-relation"),
    PackageSummary(
            JigDocumentLabel.of("パッケージ概要", "PackageSummary"),
            "package"),

    /**
     * ビジネスルール関連図
     *
     * ビジネスルール間の関連を可視化する。
     * クラス名と依存線のみのクラス図。ある程度以上の規模になると大きくなりすぎて使いづらくなる。
     * パッケージ関連図で把握できない場合の補助に使用する。
     */
    BusinessRuleRelationDiagram(
            JigDocumentLabel.of("ビジネスルール関連図", "BusinessRuleRelationDiagram"),
            "business-rule-relation"),

    /**
     * 区分図
     *
     * 区分と区分値を可視化する。
     * 区分の充実はドメインの把握具合と密接に関わる。
     */
    CategoryDiagram(
            JigDocumentLabel.of("区分図", "CategoryDiagram"),
            "category"),

    /**
     * 区分使用図
     *
     * 区分を使用しているクラスを可視化する。
     */
    CategoryUsageDiagram(
            JigDocumentLabel.of("区分使用図", "CategoryUsageDiagram"),
            "category-usage"),

    /**
     * 機能一覧
     *
     * 機能を提供するメソッドの一覧。
     * 三層（プレゼンテーション層、アプリケーション層、データソース層）の情報を提供する。
     * アプリケーションの状況把握に使用できる。
     *
     * 制限事項: {@link MyBatisMyBatisStatementsReader}
     */
    ApplicationList(
            JigDocumentLabel.of("機能一覧", "ApplicationList"),
            "application"),

    /**
     * サービスメソッド呼び出し図
     *
     * サービスクラスのメソッド呼び出しを可視化する。
     */
    ServiceMethodCallHierarchyDiagram(
            JigDocumentLabel.of("サービスメソッド呼び出し図", "ServiceMethodCallHierarchyDiagram"),
            "service-method-call-hierarchy"),

    /**
     * ドメイン概要
     */
    DomainSummary(
            JigDocumentLabel.of("ドメイン概要", "domain"),
            "domain"),

    /**
     * アプリケーション概要
     */
    ApplicationSummary(
            JigDocumentLabel.of("アプリケーション概要", "application"),
            "application"),
    /**
     * ユースケース概要
     */
    UsecaseSummary(
            JigDocumentLabel.of("ユースケース概要", "usecase"),
            "usecase"),

    EntrypointSummary(
            JigDocumentLabel.of("エントリーポイント概要", "entrypoint"),
            "entrypoint"),

    /**
     * 区分概要
     */
    EnumSummary(
            JigDocumentLabel.of("区分概要", "enum"),
            "enum"),

    /**
     * インサイト
     */
    Insight(JigDocumentLabel.of("インサイト", "insight"),
            "insight"
    ),

    /**
     * 用語集
     */
    TermList(
            JigDocumentLabel.of("用語集", "term"),
            "term"),
    Glossary(
            JigDocumentLabel.of("用語集", "glossary"),
            "glossary");

    private final JigDocumentLabel label;
    private final String documentFileName;

    JigDocument(JigDocumentLabel label, String documentFileName) {
        this.label = label;
        this.documentFileName = documentFileName;
    }

    public static List<JigDocument> canonical() {
        return Arrays.stream(values())
                .toList();
    }

    public String fileName() {
        return documentFileName;
    }

    public static List<JigDocument> resolve(String diagramTypes) {
        return Arrays.stream(diagramTypes.split(","))
                .map(
                        JigDocument::valueOf)
                .toList();
    }

    public String label() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage().equals("en") ? label.english : label.japanese;
    }
}
