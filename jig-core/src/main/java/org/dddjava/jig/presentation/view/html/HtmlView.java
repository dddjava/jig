package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTMLで出力する
 */
public class HtmlView {

    private final TemplateEngine templateEngine;
    private final Map<String, Object> contextMap;

    public HtmlView(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.contextMap = new ConcurrentHashMap<>();
    }

    protected void write(JigDocumentWriter jigDocumentWriter) {
        contextMap.put("title", jigDocumentWriter.jigDocument().label());
        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
    }

    protected void putContext(String key, Object variable) {
        contextMap.put(key, variable);
    }
}
