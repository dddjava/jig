package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.documents.JigDocument;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 設定ソース1層分の中間表現。
 *
 * {@link Optional#empty()} は「このソースでは値を指定していない（下位ソースに委ねる）」を意味する。
 * {@code null} や空文字を渡されたケースは、{@link Builder} 側で {@code Optional.empty()} に正規化する。
 */
public record PartialJigSettings(
        Optional<Path> outputDirectory,
        Optional<String> domainPattern,
        Optional<List<JigDocument>> documentTypes,
        Optional<Locale> locale
) {

    public static final PartialJigSettings EMPTY = new PartialJigSettings(
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Optional<Path> outputDirectory = Optional.empty();
        private Optional<String> domainPattern = Optional.empty();
        private Optional<List<JigDocument>> documentTypes = Optional.empty();
        private Optional<Locale> locale = Optional.empty();

        public Builder outputDirectory(Path value) {
            this.outputDirectory = Optional.ofNullable(value);
            return this;
        }

        public Builder outputDirectoryFromString(String value) {
            this.outputDirectory = (value == null || value.isEmpty())
                    ? Optional.empty()
                    : Optional.of(Path.of(value));
            return this;
        }

        public Builder domainPattern(String value) {
            this.domainPattern = (value == null || value.isEmpty())
                    ? Optional.empty()
                    : Optional.of(value);
            return this;
        }

        public Builder documentTypes(List<JigDocument> value) {
            this.documentTypes = (value == null || value.isEmpty())
                    ? Optional.empty()
                    : Optional.of(List.copyOf(value));
            return this;
        }

        public Builder locale(Locale value) {
            this.locale = Optional.ofNullable(value);
            return this;
        }

        public PartialJigSettings build() {
            return new PartialJigSettings(outputDirectory, domainPattern, documentTypes, locale);
        }
    }
}
