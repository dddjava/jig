package jig.classlist.infrastructure.writer;

import jig.classlist.ReportWriter;
import jig.domain.model.report.ReportRow;
import jig.domain.model.report.Reports;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class ExcelWriter implements ReportWriter {

    private static final Logger logger = Logger.getLogger(ExcelWriter.class.getName());

    @Override
    public void writeTo(Reports reports, Path output) {
        try (Workbook book = new XSSFWorkbook();
             OutputStream os = Files.newOutputStream(output)) {

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
                        0, sheet.getRow(0).getLastCellNum()
                ));
            });

            book.write(os);
            logger.info(output.toAbsolutePath() + "を出力しました。");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeRow(ReportRow reportRow, Row row) {
        reportRow.list().forEach(item -> {
            short lastCellNum = row.getLastCellNum();
            Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);

            if (item.length() > 10000) {
                logger.info("10,000文字を超えたので切り詰めます。");
                cell.setCellValue(item.substring(0, 10000) + "...(省略されました）");
            } else {
                cell.setCellValue(item);
            }
        });
    }
}
