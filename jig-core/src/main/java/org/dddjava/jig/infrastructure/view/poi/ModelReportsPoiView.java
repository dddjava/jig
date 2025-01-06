package org.dddjava.jig.infrastructure.view.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReport;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReports;
import org.dddjava.jig.infrastructure.view.poi.report.ReportItemFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView {

    private final JigDocument jigDocument;
    private final ReportItemFormatter reportItemFormatter;

    public ModelReportsPoiView(JigDocument jigDocument, JigDocumentContext jigDocumentContext) {
        this.jigDocument = jigDocument;
        this.reportItemFormatter = new ReportItemFormatter(jigDocumentContext);
    }

    public List<Path> write(Path outputDirectory, ModelReports model) throws IOException {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);
        render(model, jigDocumentWriter);
        return jigDocumentWriter.outputFilePaths();
    }

    public void render(ModelReports modelReports, JigDocumentWriter jigDocumentWriter) throws IOException {
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
