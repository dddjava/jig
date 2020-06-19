package org.dddjava.jig.presentation.view.poi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.poi.report.Header;
import org.dddjava.jig.presentation.view.poi.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView implements JigView<ModelReports> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelReportsPoiView.class);
    private final ConvertContext convertContext;

    public ModelReportsPoiView(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public void render(ModelReports modelReports, JigDocumentWriter jigDocumentWriter) throws IOException {
        try (Workbook book = new XSSFWorkbook()) {
            List<ModelReport<?>> list = modelReports.list();
            for (ModelReport<?> modelReport : list) {
                Sheet sheet = book.createSheet(modelReport.title());
                writeHeader(modelReport.header(), sheet.createRow(0));

                for (ReportRow row : modelReport.rows(convertContext)) {
                    writeRow(row, sheet.createRow(sheet.getLastRowNum() + 1));
                }

                int columns = modelReport.header().size();
                for (int i = 0; i < columns; i++) {
                    sheet.autoSizeColumn(i);
                }
                sheet.setAutoFilter(new CellRangeAddress(
                        0, sheet.getLastRowNum(),
                        0, columns - 1
                ));
            }

            jigDocumentWriter.writeXlsx(book::write);
        }
    }

    private void writeHeader(Header header, Row row) {
        for (int i = 0; i < header.size(); i++) {
            // headerは全てSTRINGで作る
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(header.textOf(i));
        }
    }

    private void writeRow(ReportRow reportRow, Row row) {
        reportRow.list().forEach(item -> {
            short lastCellNum = row.getLastCellNum();
            Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);

            if (item.length() > 10000) {
                LOGGER.info("セル(row={}, column={})に出力する文字数が10,000文字を超えています。全ての文字は出力されません。", cell.getRowIndex(), cell.getColumnIndex());
                cell.setCellValue(item.substring(0, 10000) + "...(省略されました）");
            } else {
                cell.setCellValue(item);
            }
        });
    }
}
