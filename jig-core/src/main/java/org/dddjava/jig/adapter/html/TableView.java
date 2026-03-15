package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class TableView {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public TableView(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.Glossary)
    public List<Path> invoke(JigRepository jigRepository, JigDocument jigDocument) {
        var glossary = jigService.glossary(jigRepository);
        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("glossaryData", buildJson(glossary));

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(Glossary glossary) {
        String termsJson = glossary.list().stream()
                .map(term -> Json.object("title", term.title())
                        .and("simpleText", term.simpleText())
                        .and("fqn", term.id().asText())
                        .and("kind", term.termKind().name())
                        .and("description", term.description())
                        .build())
                .collect(Collectors.joining(",", "[", "]"));

        return """
                {"terms": %s}
                """.formatted(termsJson).trim();
    }

}
