package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.report.ReportItem;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;

import java.util.Arrays;
import java.util.List;

public class Handlers {

    List<ItemHandler> itemHandlers;

    public Handlers(ConvertContext convertContext) {
        this.itemHandlers = Arrays.asList(
                new BooleanHandler(convertContext),
                new MethodDeclarationHandler(convertContext),
                new MethodDeclarationsHandler(convertContext),
                new MethodHandler(convertContext),
                new StringHandler(convertContext),
                new TypeIdentifierHandler(convertContext),
                new TypeIdentifiersHandler(convertContext)
        );
    }

    public String handle(ReportItem reportItem, Object obj) {
        for (ItemHandler itemHandler : itemHandlers) {
            if (itemHandler.canHandle(obj)) {
                return itemHandler.handle(reportItem, obj);
            }
        }

        throw new IllegalArgumentException(reportItem.name());
    }
}
