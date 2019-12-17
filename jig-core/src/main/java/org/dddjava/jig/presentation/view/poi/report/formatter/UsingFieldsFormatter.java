package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingFields;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class UsingFieldsFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    UsingFieldsFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof UsingFields;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        UsingFields usingFields = (UsingFields) item;
        switch (itemCategory) {
            case 使用しているフィールドの型:
                return usingFields.typeIdentifiers().asSimpleText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
