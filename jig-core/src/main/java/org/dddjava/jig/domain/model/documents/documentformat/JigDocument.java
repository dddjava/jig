package org.dddjava.jig.domain.model.documents.documentformat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * 取り扱うドキュメントの種類
 */
public enum JigDocument {

    PackageSummary(
            JigDocumentLabel.of("パッケージ概要", "PackageSummary"),
            "package"),

    /**
     * 一覧出力
     *
     * 一覧をHTMLで出力する。
     */
    ListOutput(
            JigDocumentLabel.of("一覧出力", "ListOutput"),
            "list-output"),

    /**
     * ドメイン概要
     */
    DomainSummary(
            JigDocumentLabel.of("ドメイン概要", "domain"),
            "domain"),

    /**
     * ユースケース概要
     */
    UsecaseSummary(
            JigDocumentLabel.of("ユースケース概要", "usecase"),
            "usecase"),

    EntrypointSummary(
            JigDocumentLabel.of("入力インタフェース概要", "inbound"),
            "inbound"),

    /**
     * 出力インタフェース概要
     */
    OutputsSummary(
            JigDocumentLabel.of("出力インタフェース概要", "outbound"),
            "outbound"),

    /**
     * インサイト
     */
    Insight(JigDocumentLabel.of("インサイト", "insight"),
            "insight"
    ),

    /**
     * 用語集
     */
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
