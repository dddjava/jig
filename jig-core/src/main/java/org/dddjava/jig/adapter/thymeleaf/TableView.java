package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class TableView {

    private final JigDocument jigDocument;
    private final TemplateEngine templateEngine;

    public TableView(JigDocument jigDocument, TemplateEngine templateEngine) {
        this.jigDocument = jigDocument;
        this.templateEngine = templateEngine;
    }

    public List<Path> write(Path outputDirectory, Glossary glossary) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

        String termsJson = glossary.list().stream()
                .map(term -> Json.object("title", term.title())
                        .and("simpleText", term.simpleText())
                        .and("fqn", term.id().asText())
                        .and("kind", term.termKind().name())
                        .and("description", term.description())
                        .build())
                .collect(Collectors.joining(",", "[", "]"));

        String glossaryJson = """
                {"terms": %s}
                """.formatted(termsJson).trim();

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label()
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String fileName = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(fileName, context, writer));

        jigDocumentWriter.write(
                outputStream -> {
                    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                        writer.write("globalThis.glossaryData = " + glossaryJson);
                    }
                },
                "data/" + fileName + "-data.js"
        );
        return jigDocumentWriter.outputFilePaths();
    }

}
