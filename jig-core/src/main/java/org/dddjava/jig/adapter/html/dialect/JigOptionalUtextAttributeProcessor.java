package org.dddjava.jig.adapter.html.dialect;

import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.expression.StandardExpressions;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Optional;

class JigOptionalUtextAttributeProcessor extends AbstractAttributeTagProcessor {

    public static final String ATTR_NAME = "opt-utext";
    public static final int PRECEDENCE = 10000;

    protected JigOptionalUtextAttributeProcessor(TemplateMode templateMode, String dialectPrefix) {
        super(templateMode, dialectPrefix, null, false, ATTR_NAME, true, PRECEDENCE, true);
    }

    @Override
    protected final void doProcess(
            ITemplateContext context,
            IProcessableElementTag tag,
            AttributeName attributeName,
            String attributeValue,
            IElementTagStructureHandler structureHandler) {
        var configuration = context.getConfiguration();
        var expressionParser = StandardExpressions.getExpressionParser(configuration);
        var expression = expressionParser.parseExpression(context, attributeValue);

        var expressionResult = expression.execute(context);

        if (expressionResult instanceof Optional<?> optional) {
            optional.ifPresentOrElse(
                    text -> {
                        // escapeするタグを作るときはここで HtmlEscape.escapeHtml4Xml(input) とかをかける。
                        // StandardTextTagProcessor参照
                        structureHandler.setBody(text.toString(), false);
                    },
                    () -> {
                        structureHandler.removeElement();
                    });
        }
    }
}
