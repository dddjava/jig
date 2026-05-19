package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.domain.model.data.git.GitRepositoryInfo;

import java.io.BufferedWriter;
import java.io.IOException;
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

    private void write(Map<JigDocument, String> documentLinks, Path outputDirectory, Locale locale) {
        String title = "JIG";
        String jigVersion = resolveJigVersion();
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String timestampText = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"").append(locale.getLanguage()).append("\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\"/>\n");
        html.append("    <link href=\"./assets/style.css\" rel=\"stylesheet\">\n");
        html.append("    <link rel=\"icon\" href=\"./assets/favicon.ico\">\n");
        html.append("    <title>").append(title).append("</title>\n");
        html.append("</head>\n");
        html.append("<body class=\"index\">\n");
        html.append("\n");
        html.append("<header>\n");
        html.append("    <h1>").append(title).append("</h1>\n");
        html.append("    <p>出力日時: <span id=\"jig-timestamp\" data-jig-timestamp=\"").append(timestampText).append("\">").append(timestampText).append("</span>");
        html.append(" Version: ").append(jigVersion);
        html.append("</p>\n");
        html.append("    <p id=\"jig-source\"></p>\n");
        html.append("</header>\n");
        html.append("\n");
        html.append("<main>\n");

        html.append("    <section id=\"document-links\">\n");
        html.append("        <ul></ul>\n");
        html.append("    </section>\n");

        if (documentLinks.containsKey(JigDocument.PackageRelation)) {
            html.append("    <section id=\"diagrams\">\n");
            html.append("        <h2>主要パッケージ関連図</h2>\n");
            html.append("        <div id=\"package-diagram\"></div>\n");
            html.append("    </section>\n");
        }

        html.append("</main>\n");
        html.append("\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/marked@15.0.7/marked.min.js\"></script>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/mermaid@11.12.0/dist/mermaid.min.js\"></script>\n");
        html.append("    <script src=\"./assets/jig-bundle.js\"></script>\n");
        html.append("    <script src=\"./data/navigation-data.js\"></script>\n");
        html.append("    <script src=\"./data/summary-data.js\"></script>\n");
        html.append("    <script src=\"./data/package-data.js\"></script>\n");
        html.append("    <script src=\"./data/glossary-data.js\"></script>\n");
        html.append("    <script src=\"./assets/index.js\"></script>\n");
        html.append("\n");
        html.append("</body>\n");
        html.append("</html>\n");

        Path outputFilePath = indexFilePath(outputDirectory);
        try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath, StandardCharsets.UTF_8)) {
            writer.write(JigDocumentWriter.applyAssetVersion(html.toString()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * クライアントで切り替えるためのサポート言語。先頭が日本語（カノニカルキー）。
     */
    private static final List<Locale> SUPPORTED_LOCALES = List.of(Locale.JAPANESE, Locale.ENGLISH);

    private void writeNavigationData(Map<JigDocument, String> documentLinks, Path outputDirectory, Locale locale) {
        try {
            Path dataDirectory = outputDirectory.resolve("data");
            Files.createDirectories(dataDirectory);

            // links のラベルは日本語（カノニカルキー）で出力し、クライアント i18n が言語切替時に翻訳する。
            List<JsonObjectBuilder> linkObjects = Arrays.stream(JigDocument.values())
                    .map(doc -> Optional.ofNullable(documentLinks.get(doc))
                            .map(href -> Json.object("href", href).and("label", doc.label(Locale.JAPANESE))))
                    .flatMap(Optional::stream)
                    .toList();

            List<String> availableLocales = SUPPORTED_LOCALES.stream()
                    .map(Locale::toLanguageTag)
                    .toList();

            String json = Json.object("locale", locale.toLanguageTag())
                    .and("availableLocales", Json.array(availableLocales))
                    .and("links", Json.arrayObjects(linkObjects))
                    .and("translations", documentLabelTranslationsAll())
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

    /**
     * 言語コード→（日本語キー→当該言語値）の翻訳マップを返す。
     * JS 側の i18n 辞書と重複定義しないため、サーバ側を唯一のソースにする。
     * ja は恒等のため省略する。
     */
    private JsonObjectBuilder documentLabelTranslationsAll() {
        JsonObjectBuilder root = Json.object();
        for (Locale locale : SUPPORTED_LOCALES) {
            if (locale.getLanguage().equals("ja")) continue;
            JsonObjectBuilder dict = Json.object();
            for (JigDocument doc : JigDocument.values()) {
                dict.and(doc.label(Locale.JAPANESE), doc.label(locale));
            }
            root.and(locale.getLanguage(), dict);
        }
        return root;
    }

    public static Path indexFilePath(Path outputDirectory) {
        return outputDirectory.resolve(INDEX_FILE_NAME);
    }
}
