package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.implementation.declaration.type.Type;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

public class TypeHandler implements ItemHandler {

    ConvertContext convertContext;

    public TypeHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof Type;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        Type type = (Type) obj;
        return new TypeIdentifierHandler(convertContext).handle(item, type.identifier());
    }
}
