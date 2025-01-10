package org.dddjava.jig.adapter.html;

import org.dddjava.jig.HandleResult;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class IndexView {

    private final Map<String, Object> contextMap;
    private final TemplateEngine templateEngine;
    private final JigDiagramFormat diagramFormat;

    public IndexView(TemplateEngine templateEngine, JigDiagramFormat diagramFormat) {
        this.templateEngine = templateEngine;
        this.diagramFormat = diagramFormat;
        this.contextMap = new HashMap<>();
    }

    public void render(List<HandleResult> handleResultList, Path outputDirectory) {
        var diagrams = new ArrayList<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.isOutputDiagram()) {
                    diagrams.add(new DiagramComponent(handleResult.jigDocument(), list));
                } else {
                    contextMap.put(handleResult.jigDocument().name(), list.get(0));
                }
            }
        }

        contextMap.put("diagramFormat", diagramFormat);
        contextMap.put("diagrams", diagrams);
        write(outputDirectory);
    }

    private void write(Path outputDirectory) {
        contextMap.put("title", "JIG");
        contextMap.put("timestamp", ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        Path outputFilePath = outputDirectory.resolve("index.html");
        try (OutputStream out = Files.newOutputStream(outputFilePath);
             OutputStream outputStream = new BufferedOutputStream(out);
             Writer writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8)
        ) {
            templateEngine.process("index", new Context(Locale.ROOT, contextMap), writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
