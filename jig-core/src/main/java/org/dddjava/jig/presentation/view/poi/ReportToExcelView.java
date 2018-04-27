package org.dddjava.jig.presentation.view.poi;

import org.dddjava.jig.domain.model.report.ReportRow;
import org.dddjava.jig.domain.model.report.Reports;
import org.dddjava.jig.presentation.view.AbstractLocalView;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class ReportToExcelView extends AbstractLocalView {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportToExcelView.class);
    private final Reports reports;

    public ReportToExcelView(Reports reports) {
        super("jig-report_class-list.xlsx");
        this.reports = reports;
    }

    @Override
    protected void write(OutputStream outputStream) throws IOException {
        try (Workbook book = new XSSFWorkbook()) {
            reports.each(report -> {
                Sheet sheet = book.createSheet(report.title().value());
                writeRow(report.headerRow(), sheet.createRow(0));

                for (ReportRow row : report.rows()) {
                    writeRow(row, sheet.createRow(sheet.getLastRowNum() + 1));
                }

                for (int i = 0; i < report.headerRow().list().size(); i++) {
                    sheet.autoSizeColumn(i);
                }
                sheet.setAutoFilter(new CellRangeAddress(
                        0, sheet.getLastRowNum(),
                        0, sheet.getRow(0).getLastCellNum() - 1
                ));
            });
            book.write(outputStream);
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
