package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodDeclarations;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class MethodDeclarationsHandler implements ItemHandler {

    ConvertContext convertContext;

    MethodDeclarationsHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof MethodDeclarations;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        MethodDeclarations methodDeclarations = (MethodDeclarations) obj;
        switch (item) {
            case メソッド数:
                return methodDeclarations.number().asText();
            case メソッド一覧:
                return methodDeclarations.asSignatureAndReturnTypeSimpleText();
        }

        throw new IllegalArgumentException(item.name());
    }
}
