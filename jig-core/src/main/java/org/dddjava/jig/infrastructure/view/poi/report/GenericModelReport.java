package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.*;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GenericModelReport<T> implements ModelReportInterface {
    private static final Logger logger = LoggerFactory.getLogger(GenericModelReport.class);

    private final String sheetName;
    private final List<Map.Entry<String, Function<T, Object>>> reporter;
    private final List<T> items;

    public GenericModelReport(String sheetName, List<Map.Entry<String, Function<T, Object>>> reporter, List<T> items) {
        this.sheetName = sheetName;
        this.reporter = reporter;
        this.items = items;
    }

    @Override
    public void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter) {
        if (items.isEmpty()) {
            JigDocument jigDocument = jigDocumentWriter.jigDocument();
            logger.info("[{}] 出力する情報がないため、{}/{}の出力を抑止します。", jigDocument, jigDocument.label(), sheetName);
            return;
        }
        writeSheet(book, sheetName, reporter, items);
    }

    @Override
    public boolean nothing() {
        return items.isEmpty();
    }

    private void writeSheet(Workbook book, String sheetName, List<Map.Entry<String, Function<T, Object>>> reporter, List<T> items) {
        Sheet sheet = book.createSheet(sheetName);
        writeHeader(sheet, reporter.stream().map(Map.Entry::getKey).toList());
        List<Function<T, Object>> bodyFunctions = reporter.stream().map(Map.Entry::getValue).toList();

        items.forEach(item -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            for (var i = 0; i < bodyFunctions.size(); i++) {
                var value = bodyFunctions.get(i).apply(item);
                if (value instanceof String stringValue) {
                    Cell cell = row.createCell(i, CellType.STRING);
                    if (stringValue.length() > 10000) {
                        logger.info("セル(row={}, column={})に出力する文字数が10,000文字を超えています。全ての文字は出力されません。",
                                cell.getRowIndex(), cell.getColumnIndex());
                        stringValue = stringValue.substring(0, 10000) + "...(省略されました）";
                    }
                    cell.setCellValue(stringValue);
                } else if (value instanceof Number numberValue) {
                    Cell cell = row.createCell(i, CellType.NUMERIC);
                    cell.setCellValue(numberValue.doubleValue());
                } else {
                    throw new UnsupportedOperationException("unsupported type " + value.getClass().getSimpleName());
                }
            }
        });

        applyAttribute(sheet, reporter.size());
    }

    private void writeHeader(Sheet sheet, List<String> header) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < header.size(); i++) {
            // headerは全てSTRINGで作る
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(header.get(i));
        }
    }
}
