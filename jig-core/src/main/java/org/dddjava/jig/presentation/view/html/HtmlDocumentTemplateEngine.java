package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

class HtmlDocumentTemplateEngine {

    TemplateEngine templateEngine = new TemplateEngine();

    public HtmlDocumentTemplateEngine() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");
        templateEngine.setTemplateResolver(templateResolver);
    }

    public HtmlDocumentTemplateEngine(JigDocumentContext jigDocumentContext) {
        this();
        templateEngine.addDialect(new JigExpressionObjectDialect(jigDocumentContext));
    }

    public String process(JigDocumentWriter jigDocumentWriter, Map<String, Object> contextMap) {
        Context context = new Context(Locale.ROOT, contextMap);
        return templateEngine.process(jigDocumentWriter.jigDocument().fileName(), context);
    }
}