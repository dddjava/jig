package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.models.applications.inputs.Entrypoint;
import org.dddjava.jig.domain.model.models.domains.term.Terms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ModelReportInterface {

    void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter);

    boolean nothing();

    static ModelReportInterface fromInputs(Entrypoint entrypoint) {
        return new ModelReportInterface() {

            @Override
            public void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter) {
                writeSheet(book, "CONTROLLER", Entrypoint.reporter(), entrypoint.listRequestHandlerMethods());
            }

            @Override
            public boolean nothing() {
                return entrypoint.listRequestHandlerMethods().isEmpty();
            }
        };
    }

    static ModelReportInterface fromTerm(Terms terms) {
        return new ModelReportInterface() {

            @Override
            public void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter) {
                writeSheet(book, "TERM", Terms.reporter(), terms.list());
            }

            @Override
            public boolean nothing() {
                return terms.list().isEmpty();
            }
        };
    }

    default <T> void writeSheet(Workbook book, String sheetName, List<Map.Entry<String, Function<T, Object>>> reporter, List<T> items) {
        Sheet sheet = book.createSheet(sheetName);
        writeHeader(sheet, reporter.stream().map(Map.Entry::getKey).toList());
        List<Function<T, Object>> bodyFunctions = reporter.stream().map(Map.Entry::getValue).toList();

        items.forEach(item -> {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            for (var i = 0; i < bodyFunctions.size(); i++) {
                var value = bodyFunctions.get(i).apply(item);
                if (value instanceof String stringValue) {
                    Cell cell = row.createCell(i, CellType.STRING);
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

    default void writeHeader(Sheet sheet, List<String> header) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < header.size(); i++) {
            // headerは全てSTRINGで作る
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(header.get(i));
        }
    }

    default void applyAttribute(Sheet sheet, int columns) {
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
