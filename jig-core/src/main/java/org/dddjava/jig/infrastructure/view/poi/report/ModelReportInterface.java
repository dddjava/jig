package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.models.domains.term.Terms;

import java.util.List;

public interface ModelReportInterface {

    void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter);

    boolean nothing();

    static ModelReportInterface fromTerm(Terms terms) {
        return new ModelReportInterface() {

            @Override
            public void writeSheet(Workbook book, JigDocumentWriter jigDocumentWriter, ReportItemFormatter reportItemFormatter) {
                Sheet sheet = book.createSheet("TERM");
                var header = List.of("用語（英名）", "用語", "説明", "種類", "識別子");
                writeHeader(sheet, header);

                terms.list().forEach(term -> {
                    Row row = sheet.createRow(sheet.getLastRowNum() + 1);

                    var body = List.of(
                            term.identifier().simpleText(),
                            term.title(),
                            term.description(),
                            term.termKind().toString(),
                            term.identifier().asText()
                    );
                    for (var i = 0; i < body.size(); i++) {
                        Cell cell = row.createCell(i, CellType.STRING);
                        cell.setCellValue(body.get(i));
                    }
                });

                applyAttribute(sheet, header.size());
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
