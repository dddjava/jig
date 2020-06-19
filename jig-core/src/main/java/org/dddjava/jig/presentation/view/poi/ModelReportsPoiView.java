package org.dddjava.jig.presentation.view.poi;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.Header;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.poi.report.formatter.ReportItemFormatters;

import java.io.IOException;
import java.util.List;

/**
 * ModelReportをPOIで一覧出力するView
 */
public class ModelReportsPoiView implements JigView<ModelReports> {

    private ReportItemFormatters reportItemFormatters;

    public ModelReportsPoiView(ConvertContext convertContext) {
        this.reportItemFormatters = new ReportItemFormatters(convertContext);
    }

    @Override
    public void render(ModelReports modelReports, JigDocumentWriter jigDocumentWriter) throws IOException {

        try (Workbook book = new XSSFWorkbook()) {
            List<ModelReport<?>> list = modelReports.list();
            for (ModelReport<?> modelReport : list) {
                Sheet sheet = book.createSheet(modelReport.title());
                writeHeader(modelReport.header(), sheet.createRow(0));

                modelReport.apply(sheet, reportItemFormatters);

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
}
