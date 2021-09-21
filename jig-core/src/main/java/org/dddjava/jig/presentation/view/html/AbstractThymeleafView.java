package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractThymeleafView {

    final Map<String, Object> contextMap;
    final TemplateEngine templateEngine;

    AbstractThymeleafView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.contextMap = new ConcurrentHashMap<>();
    }

    protected void write(JigDocumentWriter jigDocumentWriter) {
        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }

    protected void putContext(String key, Object variable) {
        contextMap.put(key, variable);
    }
}
