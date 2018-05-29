package org.dddjava.jig.presentation.view.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.domain.basic.report.ReportRow;
import org.dddjava.jig.domain.basic.report.Reports;
import org.dddjava.jig.presentation.view.JigDocumentLocation;
import org.dddjava.jig.presentation.view.JigView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.StringJoiner;

public class ReportToExcelView implements JigView<Reports> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportToExcelView.class);

    @Override
    public void render(Reports reports, JigDocumentLocation jigDocumentLocation) throws IOException {
        try (Workbook book = new XSSFWorkbook()) {
            StringJoiner debugText = new StringJoiner("\n");
            reports.each(report -> {
                Sheet sheet = book.createSheet(report.title().value());
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
            });

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
