package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.dialect.IProcessorDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Set;

public class JigDialect implements IExpressionObjectDialect, IProcessorDialect {
    JigDocumentContext jigDocumentContext;

    public JigDialect(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    /**
     * {@link org.thymeleaf.dialect.IDialect}
     */
    @Override
    public String getName() {
        return "jig-dialect";
    }

    /**
     * ExpressionObject
     * {@link IExpressionObjectDialect}
     */
    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new JigExpressionObjectFactory(jigDocumentContext);
    }

    /**
     * 属性のデフォルトプレフィックス。 `jig:xxx` の形で使用できる。
     * {@link IProcessorDialect}
     */
    @Override
    public String getPrefix() {
        return "jig";
    }

    /**
     * Processorの優先順位
     * {@link IProcessorDialect}
     */
    @Override
    public int getDialectProcessorPrecedence() {
        return 1000;
    }

    /**
     * 使用するProcessor
     * {@link IProcessorDialect}
     */
    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        return Set.of(new JigOptionalUtextAttributeProcessor(TemplateMode.HTML, dialectPrefix));
    }
}

