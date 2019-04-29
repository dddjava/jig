package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class BooleanHandler implements ItemHandler {

    ConvertContext convertContext;

    BooleanHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof Boolean;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        Boolean aBoolean = (Boolean) obj;
        switch (item) {
            case イベントハンドラ:
            case 汎用真偽値:
                return aBoolean ? "◯" : "";
        }

        throw new IllegalArgumentException(item.name());
    }
}
