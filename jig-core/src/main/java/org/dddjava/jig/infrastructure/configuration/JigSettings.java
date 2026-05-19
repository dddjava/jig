package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * JIG 実行時の確定済み設定。
 *
 * すべてのフィールドは非 null で値が確定している。未指定状態を表現する必要がある中間値は
 * {@link PartialJigSettings} を用い、最終的に {@link JigSettingsLoader} が合成して本オブジェクトを生成する。
 */
public record JigSettings(
        Path outputDirectory,
        Optional<String> domainPattern,
        List<JigDocument> documentTypes,
        Locale locale
) {
    public static JigSettings defaults() {
        return new JigSettings(
                Paths.get(System.getProperty("user.dir")).resolve(".jig").toAbsolutePath(),
                Optional.empty(),
                JigDocument.canonical(),
                Locale.JAPANESE
        );
    }
}
