package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.thymeleaf.Json;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TableView {

    private final JigDocument jigDocument;

    public TableView(JigDocument jigDocument) {
        this.jigDocument = jigDocument;
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

        String fileName = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.write(
                outputStream -> {
                    try (var resource = TableView.class.getResourceAsStream("/templates/" + fileName + ".html")) {
                        Objects.requireNonNull(resource).transferTo(outputStream);
                    }
                },
                fileName + ".html"
        );

        jigDocumentWriter.write(
                outputStream -> {
                    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
                        writer.write("globalThis.glossaryData = " + glossaryJson);
                    }
                },
                "data/" + fileName + "-data.js"
        );
        return jigDocumentWriter.outputFilePaths();
    }

}
