package org.dddjava.jig.infrastructure.view.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.application.JigView;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReport;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReports;
import org.dddjava.jig.infrastructure.view.poi.report.ReportItemFormatter;

import java.io.IOException;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView implements JigView {

    private final JigDocument jigDocument;
    private final ReportItemFormatter reportItemFormatter;

    public ModelReportsPoiView(JigDocument jigDocument, JigDocumentContext jigDocumentContext) {
        this.jigDocument = jigDocument;
        this.reportItemFormatter = new ReportItemFormatter(jigDocumentContext);
    }

    @Override
    public JigDocument jigDocument() {
        return jigDocument;
    }

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) throws IOException {
        ModelReports modelReports = (ModelReports) model;
        if (modelReports.empty()) {
            jigDocumentWriter.markSkip();
            return;
        }

        try (Workbook book = new XSSFWorkbook()) {
            List<ModelReport<?>> list = modelReports.list();
            for (ModelReport<?> modelReport : list) {
                modelReport.writeSheet(book, jigDocumentWriter, reportItemFormatter);
            }

            jigDocumentWriter.writeXlsx(book::write);
        }
    }

}
