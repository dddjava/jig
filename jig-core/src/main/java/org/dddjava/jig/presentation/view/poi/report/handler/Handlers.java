package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.method.DecisionNumber;
import org.dddjava.jig.domain.model.declaration.method.MethodNumber;
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
                new TypeIdentifiersHandler(convertContext),
                // 移行用
                new ItemHandler() {
                    @Override
                    public boolean canHandle(Object obj) {
                        return obj instanceof DecisionNumber;
                    }

                    @Override
                    public String handle(ReportItem item, Object obj) {
                        return ((DecisionNumber) obj).asText();
                    }
                },
                new ItemHandler() {
                    @Override
                    public boolean canHandle(Object obj) {
                        return obj instanceof UserNumber;
                    }

                    @Override
                    public String handle(ReportItem item, Object obj) {
                        return ((UserNumber) obj).asText();
                    }
                },
                new ItemHandler() {
                    @Override
                    public boolean canHandle(Object obj) {
                        return obj instanceof MethodNumber;
                    }

                    @Override
                    public String handle(ReportItem item, Object obj) {
                        return ((MethodNumber) obj).asText();
                    }
                }
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
