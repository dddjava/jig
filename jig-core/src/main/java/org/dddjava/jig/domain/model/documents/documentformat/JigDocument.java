package org.dddjava.jig.domain.model.documents.documentformat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * 取り扱うドキュメントの種類
 */
public enum JigDocument {

    /**
     * 用語集
     */
    Glossary(JigDocumentLabel.of("用語集", "glossary"), "glossary"),

    /**
     * パッケージ関連
     */
    PackageRelation(JigDocumentLabel.of("パッケージ関連", "PackageRelation"), "package"),

    /**
     * ドメインモデル
     */
    DomainModel(JigDocumentLabel.of("ドメインモデル", "DomainModel"), "domain"),

    /**
     * ユースケース
     */
    UsecaseProcess(JigDocumentLabel.of("ユースケース", "UsecaseProcess"), "usecase"),

    /**
     * 入力インタフェース
     */
    InboundEndpoint(JigDocumentLabel.of("入力インタフェース", "InboundEndpoint"), "inbound"),

    /**
     * 出力インタフェース
     */
    OutboundCall(JigDocumentLabel.of("出力インタフェース", "OutboundCall"), "outbound"),

    /**
     * インサイト
     */
    Insight(JigDocumentLabel.of("インサイト", "insight"), "insight"),

    /**
     * 一覧出力
     */
    ListOutput(JigDocumentLabel.of("一覧出力", "ListOutput"), "list-output");

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
