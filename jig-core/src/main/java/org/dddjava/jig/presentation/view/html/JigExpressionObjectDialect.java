package org.dddjava.jig.presentation.view.html;

import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.thymeleaf.dialect.IExpressionObjectDialect;
import org.thymeleaf.expression.IExpressionObjectFactory;

class JigExpressionObjectDialect implements IExpressionObjectDialect {
    JigDocumentContext jigDocumentContext;

    public JigExpressionObjectDialect(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
    public String getName() {
        return "jig-dialect";
    }

    @Override
    public IExpressionObjectFactory getExpressionObjectFactory() {
        return new JigExpressionObjectFactory(jigDocumentContext);
    }
}

