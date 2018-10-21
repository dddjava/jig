package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.angle.unit.method.Method;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

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
            case クラス名:
            case クラス和名:
            case メソッドシグネチャ:
            case メソッド和名:
            case メソッド戻り値の型:
            case メソッド戻り値の型の和名:
            case メソッド引数の型の和名:
                return new MethodDeclarationHandler(convertContext).handle(item, method.declaration());
            case 分岐数:
                return method.decisionNumber().asText();
        }

        throw new IllegalArgumentException(item.name());
    }
}
