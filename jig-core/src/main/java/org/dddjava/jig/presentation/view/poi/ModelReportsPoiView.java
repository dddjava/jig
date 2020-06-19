package org.dddjava.jig.presentation.view.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.poi.report.formatter.ReportItemFormatters;

import java.io.IOException;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView implements JigView<ModelReports> {

    private ReportItemFormatters reportItemFormatters;

    public ModelReportsPoiView(ConvertContext convertContext) {
        this.reportItemFormatters = new ReportItemFormatters(convertContext);
    }

    @Override
    public void render(ModelReports modelReports, JigDocumentWriter jigDocumentWriter) throws IOException {

        try (Workbook book = new XSSFWorkbook()) {
            List<ModelReport<?>> list = modelReports.list();
            for (ModelReport<?> modelReport : list) {
                modelReport.writeSheet(book, reportItemFormatters);
            }

            jigDocumentWriter.writeXlsx(book::write);
        }
    }

}
