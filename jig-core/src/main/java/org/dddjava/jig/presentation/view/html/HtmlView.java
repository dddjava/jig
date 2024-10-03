package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.presentation.view.handler.JigDocumentWriter;
import org.dddjava.jig.presentation.view.handler.JigView;
import org.dddjava.jig.presentation.view.handler.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTMLで出力する
 */
public class HtmlView implements JigView {

    protected final JigDocumentContext jigDocumentContext;
    private final TemplateEngine templateEngine;
    private final Map<String, Object> contextMap;

    public HtmlView(TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
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

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException {
        renderInternal(model);
        write(jigDocumentWriter);
    }

    protected void renderInternal(Object model) {
        if (model instanceof ModelAndView modelAndView) {
            putContext("model", modelAndView.model());
        }
    }
}
