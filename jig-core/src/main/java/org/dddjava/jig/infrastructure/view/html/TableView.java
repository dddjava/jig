package org.dddjava.jig.infrastructure.view.html;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.application.JigView;
import org.dddjava.jig.domain.model.models.domains.term.Terms;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class TableView implements JigView {

    private final TemplateEngine templateEngine;

    public TableView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
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
