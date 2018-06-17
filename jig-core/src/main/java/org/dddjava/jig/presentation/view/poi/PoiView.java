package org.dddjava.jig.presentation.view.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.presentation.view.JigDocumentLocation;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.poi.report.Report;
import org.dddjava.jig.presentation.view.poi.report.ReportRow;
import org.dddjava.jig.presentation.view.poi.report.Reporter;
import org.dddjava.jig.presentation.view.poi.report.Reporters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;

public class PoiView implements JigView<Reporters> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoiView.class);
    private final GlossaryService glossaryService;
    private final TypeIdentifierFormatter typeIdentifierFormatter;

    public PoiView(GlossaryService glossaryService, TypeIdentifierFormatter typeIdentifierFormatter) {
        this.glossaryService = glossaryService;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }

    @Override
    public void render(Reporters reporters, JigDocumentLocation jigDocumentLocation) throws IOException {
        try (Workbook book = new XSSFWorkbook()) {
            StringJoiner debugText = new StringJoiner("\n");
            List<Reporter> list = reporters.list();
            for (Reporter reporter : list) {
                Report report = reporter.toReport(glossaryService, typeIdentifierFormatter);

                Sheet sheet = book.createSheet(report.title());
                writeRow(report.headerRow(), sheet.createRow(0));
                debugText.add(sheet.getSheetName());
                debugText.add(report.headerRow().list().toString());

                for (ReportRow row : report.rows()) {
                    writeRow(row, sheet.createRow(sheet.getLastRowNum() + 1));
                    debugText.add(row.list().toString());
                }

                for (int i = 0; i < report.headerRow().list().size(); i++) {
                    sheet.autoSizeColumn(i);
                }
                sheet.setAutoFilter(new CellRangeAddress(
                        0, sheet.getLastRowNum(),
                        0, sheet.getRow(0).getLastCellNum() - 1
                ));
            }

            jigDocumentLocation.writeDocument(book::write);
            jigDocumentLocation.writeDebugText(debugText.toString());
        }
    }

    private void writeRow(ReportRow reportRow, Row row) {
        reportRow.list().forEach(item -> {
            short lastCellNum = row.getLastCellNum();
            Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);

            if (item.length() > 10000) {
                LOGGER.info("10,000文字を超えたので切り詰めます。");
                cell.setCellValue(item.substring(0, 10000) + "...(省略されました）");
            } else {
                cell.setCellValue(item);
            }
        });
    }
}
