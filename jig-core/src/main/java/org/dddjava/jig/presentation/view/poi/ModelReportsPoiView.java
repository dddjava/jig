package org.dddjava.jig.presentation.view.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.poi.report.ReportItemFormatter;

import java.io.IOException;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView implements JigView<ModelReports> {

    private ReportItemFormatter reportItemFormatter;

    public ModelReportsPoiView(ConvertContext convertContext) {
        this.reportItemFormatter = new ReportItemFormatter(convertContext);
    }

    @Override
    public void render(ModelReports modelReports, JigDocumentWriter jigDocumentWriter) throws IOException {
        if (modelReports.empty()) {
            jigDocumentWriter.markSkip();
            return;
        }

        try (Workbook book = new XSSFWorkbook()) {
            List<ModelReport<?>> list = modelReports.list();
            for (ModelReport<?> modelReport : list) {
                modelReport.writeSheet(book, reportItemFormatter);
            }

            jigDocumentWriter.writeXlsx(book::write);
        }
    }

}
