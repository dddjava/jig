package org.dddjava.jig.adapter.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReportSheet<T> {
    private static final Logger logger = LoggerFactory.getLogger(ReportSheet.class);

    private final String sheetName;
    private final List<ReportItem<T>> reporter;
    private final List<T> items;

    public ReportSheet(String sheetName, List<ReportItem<T>> reporter, List<T> items) {
        this.sheetName = sheetName;
        this.reporter = reporter;
        this.items = items;
    }

    public void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter) {
        // TODO この判定と通知はAdapterとかもっと手前でわかっている話なので移動する。itemsが空の状態でインスタンス作る必要がない。
        if (items.isEmpty()) {
            JigDocument jigDocument = jigDocumentWriter.jigDocument();
            logger.info("[{}] 出力する情報がないため、{}/{}の出力を抑止します。", jigDocument, jigDocument.label(), sheetName);
            return;
        }
        writeSheet(book, sheetName, reporter, items);
    }

    public boolean nothingToWriteContent() {
        return items.isEmpty();
    }

    private void writeSheet(Workbook book, String sheetName, List<ReportItem<T>> reporter, List<T> items) {
        Sheet sheet = book.createSheet(sheetName);
        writeHeader(sheet, reporter.stream().map(ReportItem::label).toList());

        //List<Function<T, ?>> にしたらコンパイルエラーになる。varならいける。謎。
        var bodyFunctions = reporter.stream().map(ReportItem::valueResolver).toList();

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

    public void applyAttribute(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            // 列幅を自動調整する
            sheet.autoSizeColumn(i);
        }
        // オートフィルターを有効にする
        sheet.setAutoFilter(new CellRangeAddress(
                0, sheet.getLastRowNum(),
                0, columns - 1
        ));
    }
}
