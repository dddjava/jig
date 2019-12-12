package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

public class ServiceMethodsFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    public ServiceMethodsFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof ServiceMethods;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        ServiceMethods serviceMethods = (ServiceMethods) item;
        // categoryにかかわらず出し方は一定でいい気がする
        return serviceMethods.reportText();
    }
}
