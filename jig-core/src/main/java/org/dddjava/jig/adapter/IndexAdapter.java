package org.dddjava.jig.adapter;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.JigResult;
import org.dddjava.jig.domain.model.documents.JigDocument;
import org.dddjava.jig.infrastructure.git.GitRepositoryInfo;

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

    public void render(List<HandleResult> handleResultList, Path outputDirectory) {
        render(handleResultList, outputDirectory, GitRepositoryInfo.empty(), JigResult.JigSummary.empty());
    }

    public void render(List<HandleResult> handleResultList, Path outputDirectory,
                       GitRepositoryInfo gitRepositoryInfo, JigResult.JigSummary jigSummary) {
        Map<JigDocument, String> documentLinks = new HashMap<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                documentLinks.put(handleResult.jigDocument(), handleResult.outputFileNames().get(0));
            }
        }
        write(documentLinks, outputDirectory);
        writeNavigationData(documentLinks, outputDirectory);
        writeSummaryData(gitRepositoryInfo, jigSummary, outputDirectory);
    }

    private String resolveJigVersion() {
        var implementationVersion = this.getClass().getPackage().getImplementationVersion();
        return Objects.requireNonNullElse(implementationVersion, "unknown");
    }

    private void write(Map<JigDocument, String> documentLinks, Path outputDirectory) {
        String title = "JIG";
        String jigVersion = resolveJigVersion();
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String timestampText = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ja\">\n");
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
        html.append("    <p id=\"jig-stats\"></p>\n");
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
            writer.write(html.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeNavigationData(Map<JigDocument, String> documentLinks, Path outputDirectory) {
        try {
            Path dataDirectory = outputDirectory.resolve("data");
            Files.createDirectories(dataDirectory);

            List<NavigationLink> links = new ArrayList<>();
            for (JigDocument doc : JigDocument.values()) {
                addNavigationLinkIfPresent(links, documentLinks, doc);
            }

            StringBuilder js = new StringBuilder();
            js.append("globalThis.navigationData = {\"links\": [");
            for (int i = 0; i < links.size(); i++) {
                NavigationLink link = links.get(i);
                if (i > 0) js.append(",");
                js.append("{\"href\":\"")
                        .append(escapeJson(link.href()))
                        .append("\",\"label\":\"")
                        .append(escapeJson(link.label()))
                        .append("\"}");
            }
            js.append("]};\n");

            Files.writeString(dataDirectory.resolve(NAVIGATION_DATA_JS), js.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeSummaryData(GitRepositoryInfo gitRepositoryInfo, JigResult.JigSummary jigSummary, Path outputDirectory) {
        try {
            Path dataDirectory = outputDirectory.resolve("data");
            Files.createDirectories(dataDirectory);

            StringBuilder js = new StringBuilder();
            js.append("globalThis.summaryData = {");
            js.append("\"git\":").append(gitJson(gitRepositoryInfo));
            js.append(",\"stats\":").append(statsJson(jigSummary));
            js.append("};\n");

            Files.writeString(dataDirectory.resolve(SUMMARY_DATA_JS), js.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String gitJson(GitRepositoryInfo gitRepositoryInfo) {
        if (!gitRepositoryInfo.isPresent()) return "null";
        StringBuilder json = new StringBuilder("{");
        gitRepositoryInfo.shortHash().ifPresent(hash ->
                json.append("\"shortHash\":\"").append(escapeJson(hash)).append("\""));
        gitRepositoryInfo.blobUrlPrefix().ifPresent(prefix -> {
            if (json.length() > 1) json.append(",");
            json.append("\"blobUrlPrefix\":\"").append(escapeJson(prefix)).append("\"");
        });
        gitRepositoryInfo.remoteUrl().ifPresent(remote -> {
            if (json.length() > 1) json.append(",");
            json.append("\"remote\":{");
            json.append("\"rawUrl\":\"").append(escapeJson(remote.rawUrl())).append("\"");
            remote.knownHost().ifPresent(known -> {
                json.append(",\"baseUrl\":\"").append(escapeJson(known.baseUrl())).append("\"");
                json.append(",\"displayName\":\"").append(escapeJson(known.displayName())).append("\"");
                remote.commitUrl(gitRepositoryInfo.shortHash().orElse(""))
                        .filter(url -> gitRepositoryInfo.shortHash().isPresent())
                        .ifPresent(url -> json.append(",\"commitUrl\":\"").append(escapeJson(url)).append("\""));
            });
            json.append("}");
        });
        json.append("}");
        return json.toString();
    }

    private String statsJson(JigResult.JigSummary jigSummary) {
        return "{"
                + "\"packageCount\":" + jigSummary.numberOfPackages()
                + ",\"classCount\":" + jigSummary.numberOfClasses()
                + ",\"methodCount\":" + jigSummary.numberOfMethods()
                + "}";
    }

    private void addNavigationLinkIfPresent(List<NavigationLink> links, Map<JigDocument, String> documentLinks, JigDocument key) {
        String href = documentLinks.get(key);
        if (href != null) {
            links.add(new NavigationLink(href, key.label()));
        }
    }

    private String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    record NavigationLink(String href, String label) {
    }

    public static Path indexFilePath(Path outputDirectory) {
        return outputDirectory.resolve(INDEX_FILE_NAME);
    }
}
