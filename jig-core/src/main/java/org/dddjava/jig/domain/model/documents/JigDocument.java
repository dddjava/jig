package org.dddjava.jig.domain.model.documents;

import java.util.Arrays;
import java.util.List;

/**
 * 取り扱うドキュメントの種類
 * <p>
 * ラベルは日本語をカノニカルキーとし、他言語への翻訳はクライアントサイド i18n
 * （{@code jig-i18n.js} の builtinDictionaries）に集約する。
 */
public enum JigDocument {

    /**
     * 用語集
     */
    Glossary("用語集", "glossary"),

    /**
     * パッケージ関連
     */
    PackageRelation("パッケージ関連", "package"),

    /**
     * ドメインモデル
     */
    DomainModel("ドメインモデル", "domain"),

    /**
     * ユースケース
     */
    Usecase("ユースケース", "usecase"),

    /**
     * 入力インタフェース
     */
    InboundInterface("入力インタフェース", "inbound"),

    /**
     * 出力インタフェース
     */
    OutboundInterface("出力インタフェース", "outbound"),

    /**
     * インサイト
     */
    Insight("インサイト", "insight"),

    /**
     * 一覧出力
     */
    ListOutput("一覧出力", "list-output"),

    /**
     * ライブラリ依存情報
     */
    LibraryDependency("ライブラリ依存情報", "library-dependency");

    private final String label;
    private final String documentFileName;

    JigDocument(String label, String documentFileName) {
        this.label = label;
        this.documentFileName = documentFileName;
    }

    public static List<JigDocument> canonical() {
        return Arrays.stream(values()).toList();
    }

    public String fileName() {
        return documentFileName;
    }

    public static List<JigDocument> resolve(String diagramTypes) {
        return Arrays.stream(diagramTypes.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .map(JigDocument::valueOf)
                .toList();
    }

    /**
     * 日本語ラベル（カノニカルキー）を返す。
     * 表示用の翻訳はクライアント i18n が担う。
     */
    public String label() {
        return label;
    }
}
