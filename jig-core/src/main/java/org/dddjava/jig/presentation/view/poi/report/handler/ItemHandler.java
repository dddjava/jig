package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.basic.ReportItem;

public interface ItemHandler {
    boolean canHandle(Object obj);

    String handle(ReportItem item, Object obj);
}
