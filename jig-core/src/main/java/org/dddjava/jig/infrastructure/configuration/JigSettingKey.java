package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * JIG 設定の単一の定義元（single source of truth）。
 *
 * 各設定項目の「プロパティキー」「環境変数名の導出」「生文字列のパース・検証」をここに集約する。
 * 設定ソース（{@link PropertiesFileSource} / {@link SystemPropertySource} /
 * {@link EnvironmentVariableSource} / CLI 引数）はいずれも本 enum を通じて {@link PartialJigSettings}
 * を組み立てるため、入り口によらず同一のキー集合・同一の検証が適用される。
 *
 * 不正な値は {@link #apply} で例外を投げる（fail-fast）。空文字・空白は「未指定」として
 * {@link JigSettingsRawSource} 側でスキップされ、本メソッドには渡らない。
 */
public enum JigSettingKey {
    OUTPUT_DIRECTORY("jig.output.directory") {
        @Override
        void apply(PartialJigSettings.Builder builder, String rawValue) {
            // 不正なパスは Path.of が InvalidPathException を投げる。
            builder.outputDirectory(Path.of(rawValue));
        }
    },
    DOMAIN_PATTERN("jig.pattern.domain") {
        @Override
        void apply(PartialJigSettings.Builder builder, String rawValue) {
            // 実際の利用は CoreDomainCondition だが、不正な正規表現を設定時点で弾くため早期コンパイルする。
            Pattern.compile(rawValue); // 不正なら PatternSyntaxException
            builder.domainPattern(rawValue);
        }
    },
    DOCUMENT_TYPES("jig.document.types") {
        @Override
        void apply(PartialJigSettings.Builder builder, String rawValue) {
            // 未知のドキュメント種別は JigDocument.resolve が IllegalArgumentException を投げる。
            builder.jigDocuments(JigDocument.resolve(rawValue));
        }
    },
    LOCALE("jig.locale") {
        @Override
        void apply(PartialJigSettings.Builder builder, String rawValue) {
            builder.locale(parseLocale(rawValue));
        }
    };

    private final String propertyKey;

    JigSettingKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * 生文字列をパース・検証し、{@code builder} に反映する。不正な値は例外を投げる（fail-fast）。
     */
    abstract void apply(PartialJigSettings.Builder builder, String rawValue);

    public String propertyKey() {
        return propertyKey;
    }

    /**
     * 環境変数名。{@code jig.output.directory} → {@code JIG_OUTPUT_DIRECTORY} のように導出する。
     */
    public String environmentVariableName() {
        return propertyKey.toUpperCase(Locale.ROOT).replace('.', '_');
    }

    public static Optional<JigSettingKey> fromPropertyKey(String propertyKey) {
        return Arrays.stream(values())
                .filter(key -> key.propertyKey.equals(propertyKey))
                .findFirst();
    }

    public static Optional<JigSettingKey> fromEnvironmentVariableName(String environmentVariableName) {
        return Arrays.stream(values())
                .filter(key -> key.environmentVariableName().equals(environmentVariableName))
                .findFirst();
    }

    private static Locale parseLocale(String rawValue) {
        Locale parsed = Locale.forLanguageTag(rawValue);
        if (parsed.getLanguage().isEmpty()) {
            // forLanguageTag は不正タグに対して Locale.ROOT を返すため、言語コード未設定を弾く必要がある。
            // 採用すると <html lang=""> 出力や辞書引きの不整合を招く。
            throw new IllegalArgumentException(
                    "jig.locale=\"" + rawValue + "\" は不正な言語タグです。");
        }
        return parsed;
    }
}
