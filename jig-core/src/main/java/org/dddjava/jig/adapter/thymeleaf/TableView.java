package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

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
                .map(term -> """
                        {"title": "%s", "simpleText": "%s", "fqn": "%s", "kind": "%s", "description": "%s"}
                        """.formatted(
                        escape(term.title()),
                        escape(term.simpleText()),
                        escape(term.id().asText()),
                        escape(term.termKind().name()),
                        escape(term.description())))
                .collect(Collectors.joining(",", "[", "]"));

        String glossaryJson = """
                {"terms": %s}
                """.formatted(termsJson);

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "glossaryJson", glossaryJson
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

}
