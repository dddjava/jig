package org.dddjava.jig.adapter.html.view;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
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
import java.util.stream.Collectors;

public class IndexView {
    static final String INDEX_FILE_NAME = "index.html";
    static final String NAVIGATION_DATA_JS = "navigation-data.js";

    private final Map<JigDocument, String> documentLinks;
    private final List<DiagramComponent> diagrams;
    private final JigDiagramFormat diagramFormat;

    public IndexView(JigDiagramFormat diagramFormat) {
        this.diagramFormat = diagramFormat;
        this.documentLinks = new HashMap<>();
        this.diagrams = new ArrayList<>();
    }

    public void render(List<HandleResult> handleResultList, Path outputDirectory) {
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.isOutputDiagram()) {
                    diagrams.add(new DiagramComponent(handleResult.jigDocument(), list));
                } else {
                    documentLinks.put(handleResult.jigDocument(), list.get(0));
                }
            }
        }
        write(outputDirectory);
        writeNavigationData(outputDirectory);
    }

    private String resolveJigVersion() {
        var implementationVersion = this.getClass().getPackage().getImplementationVersion();
        return Objects.requireNonNullElse(implementationVersion, "unknown");
    }

    private void write(Path outputDirectory) {
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
        if (hasAnyHtmlSummary()) {
            html.append("    <section>\n");
            html.append("        <h2>概要: HTML</h2>\n");
            html.append("        <ul>\n");
            appendLinkIfPresent(html, JigDocument.PackageSummary);
            appendLinkIfPresent(html, JigDocument.Glossary);
            appendLinkIfPresent(html, JigDocument.DomainSummary);
            appendLinkIfPresent(html, JigDocument.UsecaseSummary);
            appendLinkIfPresent(html, JigDocument.EntrypointSummary);
            appendLinkIfPresent(html, JigDocument.OutputsSummary);
            appendLinkIfPresent(html, JigDocument.Insight);
            html.append("        </ul>\n");
            html.append("    </section>\n");
        }

        // HTML一覧
        if (documentLinks.containsKey(JigDocument.ListOutput)) {
            html.append("    <section>\n");
            html.append("        <h2>一覧: HTML</h2>\n");
            html.append("        <ul>\n");
            appendLinkIfPresent(html, JigDocument.ListOutput);
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
            appendLinkIfPresent(html, JigDocument.BusinessRuleList);
            appendLinkIfPresent(html, JigDocument.ApplicationList);
            html.append("        </ul>\n");
            html.append("    </section>\n");
        }

        // 図
        if (!diagrams.isEmpty()) {
            html.append("    <section class=\"diagram\">\n");
            html.append("        <h2>図: <span>").append(diagramFormat.name()).append("</span></h2>\n");
            html.append("        <aside class=\"notice\">\n");
            html.append("            <p>2026.4.1 のリリースで廃止予定です。<a style=\"text-decoration: underline\" href=\"https://github.com/dddjava/jig/wiki/2026.4.1-%E7%94%BB%E5%83%8F%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E3%81%8A%E3%82%88%E3%81%B3Exce%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E3%83%89%E3%82%AD%E3%83%A5%E3%83%A1%E3%83%B3%E3%83%88%E3%82%92%E5%BB%83%E6%AD%A2\">詳細はこちら</a>。</p>\n");
            html.append("        </aside>\n");
            for (DiagramComponent diagram : diagrams) {
                html.append("        <div>\n");
                html.append("            <h3>").append(diagram.label()).append("</h3>\n");
                for (String image : diagram.imageFileNames()) {
                    html.append("            <a href=\"").append(image).append("\">\n");
                    html.append("                <img src=\"").append(image).append("\">\n");
                    html.append("            </a>\n");
                }
                if (diagram.hasOthers()) {
                    html.append("            <ul>\n");
                    for (String other : diagram.otherFileNames()) {
                        html.append("                <li><a href=\"").append(other).append("\">").append(other).append("</a></li>\n");
                    }
                    html.append("            </ul>\n");
                }
                html.append("        </div>\n");
            }
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

    private void writeNavigationData(Path outputDirectory) {
        try {
            Path dataDirectory = outputDirectory.resolve("data");
            Files.createDirectories(dataDirectory);

            List<NavigationLink> links = new ArrayList<>();
            // 「概要: HTML」「一覧: HTML」の順序に揃える（主要HTMLのみ）
            addNavigationLinkIfPresent(links, JigDocument.PackageSummary);
            addNavigationLinkIfPresent(links, JigDocument.Glossary);
            addNavigationLinkIfPresent(links, JigDocument.DomainSummary);
            addNavigationLinkIfPresent(links, JigDocument.UsecaseSummary);
            addNavigationLinkIfPresent(links, JigDocument.EntrypointSummary);
            addNavigationLinkIfPresent(links, JigDocument.OutputsSummary);
            addNavigationLinkIfPresent(links, JigDocument.Insight);
            addNavigationLinkIfPresent(links, JigDocument.ListOutput);

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

    private void addNavigationLinkIfPresent(List<NavigationLink> links, JigDocument key) {
        if (documentLinks.containsKey(key)) {
            links.add(new NavigationLink(documentLinks.get(key), key.label()));
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

    private boolean hasAnyHtmlSummary() {
        return documentLinks.containsKey(JigDocument.PackageSummary) ||
                documentLinks.containsKey(JigDocument.Glossary) ||
                documentLinks.containsKey(JigDocument.DomainSummary) ||
                documentLinks.containsKey(JigDocument.UsecaseSummary) ||
                documentLinks.containsKey(JigDocument.EntrypointSummary) ||
                documentLinks.containsKey(JigDocument.OutputsSummary) ||
                documentLinks.containsKey(JigDocument.Insight);
    }

    private void appendLinkIfPresent(StringBuilder html, JigDocument key) {
        if (documentLinks.containsKey(key)) {
            html.append("            <li><a href=\"").append(documentLinks.get(key)).append("\">").append(key.label()).append("</a></li>\n");
        }
    }

    public static Path indexFilePath(Path outputDirectory) {
        return outputDirectory.resolve(INDEX_FILE_NAME);
    }

    class DiagramComponent {
        JigDocument jigDocument;
        List<String> srcList;

        public DiagramComponent(JigDocument jigDocument, List<String> srcList) {
            this.jigDocument = jigDocument;
            this.srcList = srcList;
        }

        public String label() {
            return jigDocument.label();
        }

        public boolean hasOthers() {
            if (diagramFormat == JigDiagramFormat.DOT) return true;
            return srcList.stream().anyMatch(name -> !name.endsWith(diagramFormat.extension()));
        }

        public List<String> imageFileNames() {
            if (diagramFormat == JigDiagramFormat.DOT) return List.of();
            return srcList.stream().filter(name -> name.endsWith(diagramFormat.extension())).collect(Collectors.toList());
        }

        public List<String> otherFileNames() {
            if (diagramFormat == JigDiagramFormat.DOT) return srcList;
            return srcList.stream().filter(name -> !name.endsWith(diagramFormat.extension())).collect(Collectors.toList());
        }
    }
}
