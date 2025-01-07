package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ReportBook {

    List<ReportSheet<?>> sheets;

    public ReportBook(ReportSheet<?>... reporters) {
        this.sheets = Arrays.asList(reporters);
    }

    public List<Path> writeXlsx(JigDocument jigDocument, Path outputDirectory) throws IOException {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, outputDirectory);

        if (sheets.stream().allMatch(sheet -> sheet.nothingToWriteContent())) {
            jigDocumentWriter.markSkip();
            return List.of();
        }

        try (Workbook book = new XSSFWorkbook()) {
            for (ReportSheet<?> modelReportInterface : sheets) {
                modelReportInterface.writeSheet(book, jigDocumentWriter);
            }

            jigDocumentWriter.writeXlsx(book::write);
        }

        return jigDocumentWriter.outputFilePaths();
    }
}
