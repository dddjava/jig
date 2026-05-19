package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.data.git.GitRepositoryInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class IndexAdapter {
    static final String INDEX_FILE_NAME = "index.html";
    static final String NAVIGATION_DATA_JS = "navigation-data.js";
    static final String SUMMARY_DATA_JS = "summary-data.js";

    public void render(List<HandleResult> handleResultList, Path outputDirectory,
                       GitRepositoryInfo gitRepositoryInfo, Locale locale) {
        Map<JigDocument, String> documentLinks = new HashMap<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                documentLinks.put(handleResult.jigDocument(), handleResult.outputFileNames().get(0));
            }
        }
        write(documentLinks, outputDirectory, locale);
        writeNavigationData(documentLinks, outputDirectory, locale);
        writeSummaryData(gitRepositoryInfo, outputDirectory);
    }

    private String resolveJigVersion() {
        var implementationVersion = this.getClass().getPackage().getImplementationVersion();
        return Objects.requireNonNullElse(implementationVersion, "unknown");
    }

    private static final String DIAGRAMS_SECTION = """
                <section id="diagrams">
                    <h2 data-i18n>主要パッケージ関連図</h2>
                    <div id="package-diagram"></div>
                </section>""";

    private void write(Map<JigDocument, String> documentLinks, Path outputDirectory, Locale locale) {
        String timestampText = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String diagramsSection = documentLinks.containsKey(JigDocument.PackageRelation)
                ? DIAGRAMS_SECTION
                : "";

        String html = loadIndexTemplate()
                .replace("{{lang}}", locale.getLanguage())
                .replace("{{timestamp}}", timestampText)
                .replace("{{version}}", resolveJigVersion())
                .replace("{{diagrams_section}}", diagramsSection);

        try {
            Files.writeString(indexFilePath(outputDirectory),
                    JigDocumentWriter.applyAssetVersion(html), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String loadIndexTemplate() {
        try (InputStream is = JigDocumentWriter.getResourceAsStream("templates/index.html")) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeNavigationData(Map<JigDocument, String> documentLinks, Path outputDirectory, Locale locale) {
        try {
            Path dataDirectory = outputDirectory.resolve("data");
            Files.createDirectories(dataDirectory);

            // links のラベルは日本語（カノニカルキー）で出力し、クライアント i18n が翻訳する。
            // サポート言語は JS 側 builtinDictionaries に集約しているため、ここでは出力しない。
            List<JsonObjectBuilder> linkObjects = Arrays.stream(JigDocument.values())
                    .map(doc -> Optional.ofNullable(documentLinks.get(doc))
                            .map(href -> Json.object("href", href).and("label", doc.label())))
                    .flatMap(Optional::stream)
                    .toList();

            String json = Json.object("locale", locale.toLanguageTag())
                    .and("links", Json.arrayObjects(linkObjects))
                    .build();

            Files.writeString(dataDirectory.resolve(NAVIGATION_DATA_JS),
                    "globalThis.navigationData = " + json + ";\n", StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeSummaryData(GitRepositoryInfo gitRepositoryInfo, Path outputDirectory) {
        try {
            Path dataDirectory = outputDirectory.resolve("data");
            Files.createDirectories(dataDirectory);

            String json = Json.object("git", gitJson(gitRepositoryInfo)).build();
            String js = "globalThis.summaryData = " + json + ";\n";

            Files.writeString(dataDirectory.resolve(SUMMARY_DATA_JS), js, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Object gitJson(GitRepositoryInfo gitRepositoryInfo) {
        if (!gitRepositoryInfo.isPresent()) return Json.raw("null");
        JsonObjectBuilder builder = Json.object();
        gitRepositoryInfo.shortHash().ifPresent(hash -> builder.and("shortHash", hash));
        gitRepositoryInfo.blobUrlPrefix().ifPresent(prefix -> builder.and("blobUrlPrefix", prefix));
        gitRepositoryInfo.remoteUrl().ifPresent(remote -> builder.and("remote", remoteJson(remote, gitRepositoryInfo.shortHash())));
        return builder;
    }

    private JsonObjectBuilder remoteJson(GitRepositoryInfo.RemoteUrl remote, Optional<String> shortHash) {
        JsonObjectBuilder builder = Json.object("rawUrl", remote.rawUrl());
        remote.knownHost().ifPresent(known -> {
            builder.and("baseUrl", known.baseUrl());
            builder.and("displayName", known.displayName());
            shortHash.flatMap(remote::commitUrl).ifPresent(url -> builder.and("commitUrl", url));
        });
        return builder;
    }

    public static Path indexFilePath(Path outputDirectory) {
        return outputDirectory.resolve(INDEX_FILE_NAME);
    }
}
