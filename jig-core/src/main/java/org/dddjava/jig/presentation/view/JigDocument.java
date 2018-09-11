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
    ServiceMethodCallHierarchy("service-method-call-hierarchy"),

    /**
     * パッケージ依存ダイアグラム
     *
     * パッケージ間の依存を可視化する。
     * トップレベルからの階層(depth)で丸めて複数出力する。
     * 出力対象パッケージはモデルと判断されるもの。
     */
    PackageDependency("package-dependency"),

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
    DomainList("domain"),

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
    EnumUsage("enum-usage"),

    /**
     * 真偽値サービス関連ダイアグラム
     *
     * booleanを返すサービスと使用しているメソッドを可視化する。
     */
    BooleanService("boolean-service");

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
