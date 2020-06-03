package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigmodel.declaration.type.Type;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class TypeFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    TypeFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof Type;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        Type type = (Type) item;
        return new TypeIdentifierFormatter(convertContext).format(itemCategory, type.identifier());
    }
}
