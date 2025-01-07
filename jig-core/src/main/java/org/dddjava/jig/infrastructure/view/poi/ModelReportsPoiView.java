package org.dddjava.jig.infrastructure.view.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReportInterface;
import org.dddjava.jig.infrastructure.view.poi.report.ModelReports;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView {

    private final JigDocument jigDocument;

    public ModelReportsPoiView(JigDocument jigDocument) {
        this.jigDocument = jigDocument;
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
            List<ModelReportInterface> list = modelReports.list();
            for (ModelReportInterface modelReportInterface : list) {
                modelReportInterface.writeSheet(book, jigDocumentWriter);
            }

            jigDocumentWriter.writeXlsx(book::write);
        }
    }
}
