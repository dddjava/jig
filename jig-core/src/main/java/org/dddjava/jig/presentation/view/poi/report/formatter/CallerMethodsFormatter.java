package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.CallerMethods;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class CallerMethodsFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    CallerMethodsFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof CallerMethods;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        CallerMethods callerMethods = (CallerMethods) item;
        switch (itemCategory) {
            case 使用箇所数:
                return callerMethods.toUserNumber().asText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
