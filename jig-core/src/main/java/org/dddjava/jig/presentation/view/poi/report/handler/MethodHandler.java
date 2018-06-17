package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;

public class MethodHandler implements ItemHandler {

    ConvertContext convertContext;

    public MethodHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof Method;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        Method method = (Method) obj;
        switch (item) {
            case メソッド名:
            case メソッド和名:
            case メソッド戻り値の型:
            case メソッド戻り値の型の和名:
            case メソッド引数の型の和名:
                return new MethodDeclarationHandler(convertContext).handle(item, method.declaration());
            case 分岐数:
                return method.decisionNumber().asText();
        }

        throw new IllegalArgumentException();
    }
}
