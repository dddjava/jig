package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.angle.unit.method.UsingFields;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

public class UsingFieldsHandler implements ItemHandler {

    ConvertContext convertContext;

    public UsingFieldsHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof UsingFields;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        UsingFields usingFields = (UsingFields) obj;
        switch (item) {
            case 使用しているフィールドの型:
                return usingFields.typeIdentifiers().asSimpleText();
        }

        throw new IllegalArgumentException(item.name());
    }
}
