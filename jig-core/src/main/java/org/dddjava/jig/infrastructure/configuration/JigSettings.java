package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.documents.JigDocumentContext;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * JIG 実行時の確定済み設定。
 *
 * すべてのフィールドは非 null で値が確定している。未指定状態を表現する必要がある中間値は
 * {@link PartialJigSettings} を用い、最終的に {@link JigSettingsLoader} が合成して本オブジェクトを生成する。
 *
 * {@link #outputDirectory} はランタイム（CLI / Gradle plugin / テスト）固有の関心事のため
 * jig-core 側でデフォルトを持たない。呼び出し側がいずれかの設定ソースで指定する責任を負う。
 *
 * record accessor が {@link JigDocumentContext} の3メソッドを満たすため、ドキュメント生成の
 * コンテキストとしてもそのまま利用できる。
 */
public record JigSettings(
        Path outputDirectory,
        Optional<String> domainPattern,
        List<JigDocument> jigDocuments,
        Locale locale
) implements JigDocumentContext {

    /**
     * jig-core が持つフィールド既定値の集まり。{@link JigSettingsLoader} の最低優先度層として使われる。
     *
     * 出力先は jig-core の関心ではないため含めない（呼び出し側で必ず供給する）。
     */
    public static PartialJigSettings defaults() {
        return PartialJigSettings.builder()
                .jigDocuments(JigDocument.canonical())
                .locale(Locale.JAPANESE)
                .build();
    }
}
