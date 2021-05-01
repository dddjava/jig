package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class HtmlDocumentTemplateEngine extends TemplateEngine {

    JigDocumentContext jigDocumentContext;

    public HtmlDocumentTemplateEngine() {
        super();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");
        super.setTemplateResolver(templateResolver);
    }

    public HtmlDocumentTemplateEngine(JigDocumentContext jigDocumentContext) {
        this();

        this.jigDocumentContext = jigDocumentContext;

        super.addDialect(new IExpressionObjectDialect() {
            @Override
            public String getName() {
                return "jig-dialect";
            }

            @Override
            public IExpressionObjectFactory getExpressionObjectFactory() {
                return new IExpressionObjectFactory() {
                    @Override
                    public Set<String> getAllExpressionObjectNames() {
                        return Collections.singleton("jig");
                    }

                    @Override
                    public Object buildObject(IExpressionContext context, String expressionObjectName) {
                        return new JigDialectObject();
                    }

                    @Override
                    public boolean isCacheable(String expressionObjectName) {
                        return true;
                    }
                };
            }
        });
    }

    public String process(JigDocumentWriter jigDocumentWriter, Map<String, Object> contextMap) {
        Context context = new Context(Locale.ROOT, contextMap);
        return process(jigDocumentWriter.jigDocument().fileName(), context);
    }

    class JigDialectObject {
        public String labelText(TypeIdentifier typeIdentifier) {
            ClassComment classComment = jigDocumentContext.classComment(typeIdentifier);
            return classComment.asTextOrIdentifierSimpleText();
        }
    }
}
