package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class BooleanFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    BooleanFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof Boolean;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        Boolean aBoolean = (Boolean) item;
        switch (itemCategory) {
            case イベントハンドラ:
            case 汎用真偽値:
                return aBoolean ? "◯" : "";
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
