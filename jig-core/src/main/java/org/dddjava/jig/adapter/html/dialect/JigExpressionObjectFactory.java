package org.dddjava.jig.adapter.html.dialect;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.expression.IExpressionObjectFactory;

import java.util.Set;

class JigExpressionObjectFactory implements IExpressionObjectFactory {
    private final JigDocumentContext jigDocumentContext;

    public JigExpressionObjectFactory(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
    public Set<String> getAllExpressionObjectNames() {
        return Set.of("jig", "jigEntrypoint");
    }

    @Override
    public Object buildObject(IExpressionContext context, String expressionObjectName) {
        if (expressionObjectName.equals("jigEntrypoint")) {
            return new JigEntrypointExpressionObject();
        }
        return new JigExpressionObject(jigDocumentContext);
    }

    @Override
    public boolean isCacheable(String expressionObjectName) {
        return true;
    }
}
