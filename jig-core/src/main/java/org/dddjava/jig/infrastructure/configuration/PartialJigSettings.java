package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 設定ソース1層分の中間表現。
 *
 * 未指定状態は {@link Optional#empty()} か空コレクションで表す。{@link Builder} 側で
 * {@code null} / 空文字 を未指定に正規化する。
 */
public record PartialJigSettings(
        Optional<Path> outputDirectory,
        Optional<String> domainPattern,
        List<JigDocument> documentTypes,
        Optional<Locale> locale
) {

    public static final PartialJigSettings EMPTY = new PartialJigSettings(
            Optional.empty(), Optional.empty(), List.of(), Optional.empty());

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Path outputDirectory;
        private String domainPattern;
        private List<JigDocument> documentTypes = List.of();
        private Locale locale;

        public Builder outputDirectory(Path value) {
            this.outputDirectory = value;
            return this;
        }

        public Builder domainPattern(String value) {
            this.domainPattern = (value == null || value.isEmpty()) ? null : value;
            return this;
        }

        public Builder documentTypes(List<JigDocument> value) {
            this.documentTypes = (value == null) ? List.of() : List.copyOf(value);
            return this;
        }

        public Builder locale(Locale value) {
            this.locale = value;
            return this;
        }

        public PartialJigSettings build() {
            return new PartialJigSettings(
                    Optional.ofNullable(outputDirectory),
                    Optional.ofNullable(domainPattern),
                    documentTypes,
                    Optional.ofNullable(locale));
        }
    }
}
