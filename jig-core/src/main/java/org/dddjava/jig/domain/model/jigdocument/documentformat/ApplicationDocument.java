package org.dddjava.jig.domain.model.jigdocument.documentformat;

/**
 * アプリケーションドキュメント
 */
public enum ApplicationDocument {

    /**
     * 機能一覧
     *
     * 機能を提供するメソッドの一覧。
     * 三層（プレゼンテーション層、アプリケーション層、データソース層）の情報を提供する。
     * アプリケーションの状況把握に使用できる。
     *
     * 制限事項: {@link org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader}
     */
    ApplicationList,

    /**
     * 分岐数一覧
     *
     * メソッドごとの分岐数の一覧。
     */
    BranchList,

    /**
     * サービスメソッド呼び出しダイアグラム
     *
     * サービスクラスのメソッド呼び出しを可視化する。
     */
    ServiceMethodCallHierarchyDiagram,

    /**
     * ユースケース複合図
     */
    CompositeUsecaseDiagram;
}
