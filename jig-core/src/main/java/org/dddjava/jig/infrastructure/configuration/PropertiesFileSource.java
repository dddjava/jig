package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * {@code <configDirectory>/jig.properties} を {@link PartialJigSettings} に変換するソース。
 *
 * ファイルが存在しない場合や読み込みに失敗した場合は {@link PartialJigSettings#EMPTY} を返す。
 * 値の構文が不正な場合（例: {@code jig.locale=invalid-tag}）は当該キーのみ未指定扱いとする。
 */
public class PropertiesFileSource {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFileSource.class);

    private final Path configDirectory;

    public PropertiesFileSource(Path configDirectory) {
        this.configDirectory = configDirectory;
    }

    public PartialJigSettings read() {
        Path jigPropertiesPath = configDirectory.resolve("jig.properties");
        logger.debug("try to load {} ...", jigPropertiesPath.toAbsolutePath());
        if (!Files.exists(jigPropertiesPath)) {
            return PartialJigSettings.EMPTY;
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(jigPropertiesPath)) {
            properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.warn("fail to load {}", jigPropertiesPath, e);
            return PartialJigSettings.EMPTY;
        }
        logger.info("configuration loaded from {}", jigPropertiesPath.toAbsolutePath());

        PartialJigSettings.Builder builder = PartialJigSettings.builder()
                .outputDirectory(parsePath(properties.getProperty("jig.output.directory")))
                .domainPattern(properties.getProperty("jig.pattern.domain"))
                .documentTypes(parseDocumentTypes(properties.getProperty("jig.document.types")))
                .locale(parseLocale(properties.getProperty("jig.locale")));
        return builder.build();
    }

    private static Path parsePath(String value) {
        return (value == null || value.isEmpty()) ? null : Path.of(value);
    }

    private static List<JigDocument> parseDocumentTypes(String value) {
        if (value == null || value.isEmpty()) return List.of();
        try {
            return JigDocument.resolve(value);
        } catch (IllegalArgumentException e) {
            logger.warn("jig.document.types=\"{}\" に未知のドキュメント種別が含まれているため無視します。", value);
            return List.of();
        }
    }

    private static Locale parseLocale(String value) {
        if (value == null || value.isEmpty()) return null;
        Locale parsed = Locale.forLanguageTag(value);
        if (parsed.getLanguage().isEmpty()) {
            // forLanguageTag は不正タグに対して Locale.ROOT を返すため、言語コード未設定を弾く必要がある。
            // 採用すると <html lang=""> 出力や辞書引きの不整合を招く。
            logger.warn("jig.locale=\"{}\" は不正な言語タグです。設定を無視します。", value);
            return null;
        }
        return parsed;
    }
}
