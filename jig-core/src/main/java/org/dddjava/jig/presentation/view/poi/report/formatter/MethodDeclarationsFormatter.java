package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class MethodDeclarationsFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    MethodDeclarationsFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof MethodDeclarations;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        MethodDeclarations methodDeclarations = (MethodDeclarations) item;
        switch (itemCategory) {
            case メソッド数:
                return methodDeclarations.number().asText();
            case メソッド一覧:
                return methodDeclarations.asSignatureAndReturnTypeSimpleText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
