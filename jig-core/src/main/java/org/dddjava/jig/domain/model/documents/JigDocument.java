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

    Glossary("用語集", "glossary"),
    PackageRelation("パッケージ関連", "package"),
    DomainModel("ドメインモデル", "domain"),
    Usecase("ユースケース", "usecase"),
    InboundInterface("入力インタフェース", "inbound"),
    OutboundInterface("出力インタフェース", "outbound"),
    Insight("インサイト", "insight"),
    ListOutput("一覧出力", "list-output"),
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
