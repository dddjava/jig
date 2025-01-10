package org.dddjava.jig.adapter.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Collections;
import java.util.Set;

class JigExpressionObjectFactory implements IExpressionObjectFactory {
    private final JigDocumentContext jigDocumentContext;

    public JigExpressionObjectFactory(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return Collections.singleton("jig");
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        return new JigExpressionObject(jigDocumentContext);
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return true;
    }
}
