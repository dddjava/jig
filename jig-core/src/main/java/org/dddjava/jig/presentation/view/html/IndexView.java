package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocumentType;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexView {

    final Map<String, Object> contextMap;
    final TemplateEngine templateEngine;

    public IndexView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.contextMap = new ConcurrentHashMap<>();
    }

    public void render(List<HandleResult> handleResultList, Path outputDirectory) {
        List<String> diagramFiles = new ArrayList<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.jigDocument().jigDocumentType() == JigDocumentType.DIAGRAM) {
                    list.stream().filter(item -> !item.endsWith(".txt")).forEach(diagramFiles::add);
                } else {
                    putContext(handleResult.jigDocument().name(), list.get(0));
                }
            }
        }

        putContext("diagramFiles", diagramFiles);
        write(outputDirectory);
    }

    protected void write(Path outputDirectory) {
        contextMap.put("title", "JIG");
        Context context = new Context(Locale.ROOT, contextMap);
        String template = "index";

        String fileName = "index.html";
        Path outputFilePath = outputDirectory.resolve(fileName);
        try (OutputStream out = Files.newOutputStream(outputFilePath);
             OutputStream outputStream = new BufferedOutputStream(out);
             Writer writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        ) {
            templateEngine.process(template, context, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected void putContext(String key, Object variable) {
        contextMap.put(key, variable);
    }
}
