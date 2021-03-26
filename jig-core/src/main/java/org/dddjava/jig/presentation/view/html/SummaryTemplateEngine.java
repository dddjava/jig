package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Collections;
import java.util.Set;

public class SummaryTemplateEngine extends TemplateEngine {

    AliasFinder aliasFinder;

    public SummaryTemplateEngine(AliasFinder aliasFinder) {
        super();

        this.aliasFinder = aliasFinder;
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");

        super.setTemplateResolver(templateResolver);
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

    class JigDialectObject {
        public String labelText(TypeIdentifier typeIdentifier) {
            TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
            return typeAlias.asTextOrIdentifierSimpleText();
        }
    }
}
