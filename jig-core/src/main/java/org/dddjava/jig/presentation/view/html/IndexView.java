package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocumentType;
import org.dddjava.jig.presentation.view.handler.HandleResult;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class IndexView {

    private final Map<String, Object> contextMap;
    private final TemplateEngine templateEngine;

    public IndexView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.contextMap = new HashMap<>();
    }

    public void render(List<HandleResult> handleResultList, Path outputDirectory) {
        List<String> diagramFiles = new ArrayList<>();
        for (HandleResult handleResult : handleResultList) {
            if (handleResult.success()) {
                List<String> list = handleResult.outputFileNames();
                if (handleResult.jigDocument().jigDocumentType() == JigDocumentType.DIAGRAM) {
                    list.stream().filter(item -> !item.endsWith(".txt")).forEach(diagramFiles::add);
                } else {
                    contextMap.put(handleResult.jigDocument().name(), list.get(0));
                }
            }
        }

        contextMap.put("diagramFiles", diagramFiles);
        write(outputDirectory);
    }

    private void write(Path outputDirectory) {
        contextMap.put("title", "JIG");

        Path outputFilePath = outputDirectory.resolve("index.html");
        try (OutputStream out = Files.newOutputStream(outputFilePath);
             OutputStream outputStream = new BufferedOutputStream(out);
             Writer writer = new java.io.OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        ) {
            templateEngine.process("index", new Context(Locale.ROOT, contextMap), writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
