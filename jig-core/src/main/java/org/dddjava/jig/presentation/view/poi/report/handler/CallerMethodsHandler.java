package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.CallerMethods;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

public class CallerMethodsHandler implements ItemHandler {

    ConvertContext convertContext;

    public CallerMethodsHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof CallerMethods;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        CallerMethods callerMethods = (CallerMethods) obj;
        switch (item) {
            case 使用箇所数:
                return callerMethods.toUserNumber().asText();
        }

        throw new IllegalArgumentException(item.name());
    }
}
