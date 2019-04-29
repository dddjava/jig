package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

import java.util.Arrays;
import java.util.List;

public class Handlers {

    List<ItemHandler> itemHandlers;

    public Handlers(ConvertContext convertContext) {
        this.itemHandlers = Arrays.asList(
                new BooleanHandler(convertContext),
                new CallerMethodsHandler(convertContext),
                new MethodDeclarationHandler(convertContext),
                new MethodDeclarationsHandler(convertContext),
                new MethodHandler(convertContext),
                new StringHandler(convertContext),
                new TypeHandler(convertContext),
                new TypeIdentifierHandler(convertContext),
                new TypeIdentifiersHandler(convertContext),
                new UsingFieldsHandler(convertContext)
        );
    }

    public String handle(ReportItem reportItem, Object item) {
        for (ItemHandler itemHandler : itemHandlers) {
            if (itemHandler.canHandle(item)) {
                return itemHandler.handle(reportItem, item);
            }
        }

        throw new IllegalArgumentException(reportItem.name());
    }
}
