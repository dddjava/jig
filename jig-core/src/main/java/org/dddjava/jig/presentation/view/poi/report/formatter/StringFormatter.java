package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class StringFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    StringFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof String;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        String str = (String) item;
        switch (itemCategory) {
            case 汎用文字列:
                return str;
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
