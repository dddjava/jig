package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;

public class StringHandler implements ItemHandler {

    ConvertContext convertContext;

    public StringHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof String;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        String str = (String) obj;
        switch (item) {
            case 汎用文字列:
                return str;
        }

        throw new IllegalArgumentException(item.name());
    }
}
