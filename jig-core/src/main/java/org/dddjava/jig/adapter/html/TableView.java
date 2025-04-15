package org.dddjava.jig.adapter.html;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

public class TableView {

    private final JigDocument jigDocument;
    private final TemplateEngine templateEngine;

    public TableView(JigDocument jigDocument, TemplateEngine templateEngine) {
        this.jigDocument = jigDocument;
        this.templateEngine = templateEngine;
    }

    public List<Path> write(Path outputDirectory, Glossary glossary) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "glossary", glossary,
                "navigationLetters", navigationLetters(glossary)
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private static List<String> navigationLetters(Glossary glossary) {
        List<String> allLetters = glossary.list().stream()
                .map(term -> term.title().substring(0, 1))
                .distinct()
                .toList();
        int limit = 20;
        if (allLetters.size() <= limit) {
            return allLetters;
        }

        int step = Math.max(1, allLetters.size() / limit);
        return IntStream.range(0, allLetters.size())
                .filter(i -> i % step == 0)
                .mapToObj(allLetters::get)
                .toList();
    }
}
