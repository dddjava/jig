package org.dddjava.jig.adapter.html.view;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

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

public class IndexView {
    static final String INDEX_FILE_NAME = "index.html";
    static final String NAVIGATION_DATA_JS = "navigation-data.js";

    private static final List<JigDocument> HTML_SUMMARY_DOCUMENTS = List.of(
            JigDocument.PackageSummary,
            JigDocument.Glossary,
            JigDocument.DomainSummary,
            JigDocument.UsecaseSummary,
            JigDocument.EntrypointSummary,
            JigDocument.OutputsSummary,
            JigDocument.Insight
    );

    public void render(List<HandleResult> handleResultList, Path outputDirectory) {
        Map<JigDocument, String> documentLinks = new HashMap<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                documentLinks.put(handleResult.jigDocument(), handleResult.outputFileNames().get(0));
            }
        }
        write(documentLinks, outputDirectory);
        writeNavigationData(documentLinks, outputDirectory);
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
        html.append("</header>\n");
        html.append("\n");
        html.append("<main>\n");

        // HTML概要
        if (HTML_SUMMARY_DOCUMENTS.stream().anyMatch(documentLinks::containsKey)) {
            html.append("    <section>\n");
            html.append("        <h2>概要: HTML</h2>\n");
            html.append("        <ul>\n");
            for (JigDocument doc : HTML_SUMMARY_DOCUMENTS) {
                appendLinkIfPresent(html, documentLinks, doc);
            }
            html.append("        </ul>\n");
            html.append("    </section>\n");
        }

        // HTML一覧
        if (documentLinks.containsKey(JigDocument.ListOutput)) {
            html.append("    <section>\n");
            html.append("        <h2>一覧: HTML</h2>\n");
            html.append("        <ul>\n");
            appendLinkIfPresent(html, documentLinks, JigDocument.ListOutput);
            html.append("        </ul>\n");
            html.append("    </section>\n");
        }

        if (documentLinks.containsKey(JigDocument.PackageSummary)) {
            html.append("    <section id=\"diagrams\">\n");
            html.append("        <h2>主要パッケージ関連図</h2>\n");
            html.append("        <div id=\"package-diagram\"></div>\n");
            html.append("    </section>\n");
        }

        // Excel一覧
        if (documentLinks.containsKey(JigDocument.BusinessRuleList) || documentLinks.containsKey(JigDocument.ApplicationList)) {
            html.append("    <section>\n");
            html.append("        <h2>一覧: Excel</h2>\n");
            html.append("        <aside class=\"notice\">\n");
            html.append("            <p>2026.4.1 のリリースで廃止予定です。<a style=\"text-decoration: underline\" href=\"https://github.com/dddjava/jig/wiki/2026.4.1-%E7%94%BB%E5%83%8F%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E3%81%8A%E3%82%88%E3%81%B3Exce%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E3%83%89%E3%82%AD%E3%83%A5%E3%83%A1%E3%83%B3%E3%83%88%E3%82%92%E5%BB%83%E6%AD%A2\">詳細はこちら</a>。</p>\n");
            html.append("        </aside>\n");
            html.append("        <ul>\n");
            appendLinkIfPresent(html, documentLinks, JigDocument.BusinessRuleList);
            appendLinkIfPresent(html, documentLinks, JigDocument.ApplicationList);
            html.append("        </ul>\n");
            html.append("    </section>\n");
        }

        html.append("</main>\n");
        html.append("\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/marked@15.0.7/marked.min.js\"></script>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/mermaid@11.12.0/dist/mermaid.min.js\"></script>\n");
        html.append("    <script src=\"./assets/jig.js\"></script>\n");
        html.append("    <script src=\"./assets/jig-common.js\"></script>\n");
        html.append("    <script src=\"./assets/package-diagram.js\"></script>\n");
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
            for (JigDocument doc : HTML_SUMMARY_DOCUMENTS) {
                addNavigationLinkIfPresent(links, documentLinks, doc);
            }
            addNavigationLinkIfPresent(links, documentLinks, JigDocument.ListOutput);

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

    private void appendLinkIfPresent(StringBuilder html, Map<JigDocument, String> documentLinks, JigDocument key) {
        String href = documentLinks.get(key);
        if (href != null) {
            html.append("            <li><a href=\"").append(href).append("\">").append(key.label()).append("</a></li>\n");
        }
    }

    public static Path indexFilePath(Path outputDirectory) {
        return outputDirectory.resolve(INDEX_FILE_NAME);
    }
}
