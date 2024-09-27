package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.presentation.handler.JigDocumentWriter;
import org.dddjava.jig.presentation.handler.JigView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class TableView implements JigView {

    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public TableView(TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
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
