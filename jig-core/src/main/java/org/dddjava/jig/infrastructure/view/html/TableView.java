package org.dddjava.jig.infrastructure.view.html;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.models.domains.term.Terms;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TableView {

    private final JigDocument jigDocument;
    private final TemplateEngine templateEngine;

    public TableView(JigDocument jigDocument, TemplateEngine templateEngine) {
        this.jigDocument = jigDocument;
        this.templateEngine = templateEngine;
    }

    public JigDocument jigDocument() {
        return jigDocument;
    }

    public List<Path> write(Path outputDirectory, Object model) throws IOException {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument(), outputDirectory);
        render(model, jigDocumentWriter);
        return jigDocumentWriter.outputFilePaths();
    }

    public void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException {
        Terms terms = (Terms) model;

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "terms", terms
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }
}
