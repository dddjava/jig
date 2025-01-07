package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.models.domains.term.Term;
import org.dddjava.jig.domain.model.models.domains.term.Terms;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ModelReportInterface {

    void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter);

    boolean nothing();

    static ModelReportInterface fromTerm(Terms terms) {
        var reporter = Terms.reporter();

        return new ModelReportInterface() {

            @Override
            public void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter) {
                Sheet sheet = book.createSheet("TERM");
                writeHeader(sheet, reporter.stream().map(Map.Entry::getKey).toList());
                List<Function<Term, String>> bodyFunctions = reporter.stream().map(Map.Entry::getValue).toList();

                terms.list().forEach(term -> {
                    Row row = sheet.createRow(sheet.getLastRowNum() + 1);

                    for (var i = 0; i < bodyFunctions.size(); i++) {
                        Cell cell = row.createCell(i, CellType.STRING);
                        cell.setCellValue(bodyFunctions.get(i).apply(term));
                    }
                });

                applyAttribute(sheet, reporter.size());
            }

            @Override
            public boolean nothing() {
                return terms.list().isEmpty();
            }
        };
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
