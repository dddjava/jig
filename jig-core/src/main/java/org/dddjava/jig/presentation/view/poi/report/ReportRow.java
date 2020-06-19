package org.dddjava.jig.presentation.view.poi.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReportRow {
    static Logger logger = LoggerFactory.getLogger(ReportRow.class);

    List<String> list;

    ReportRow(List<String> list) {
        this.list = list;
    }

    public List<String> list() {
        return list;
    }

    public void writeRow(Row row) {
        list().forEach(item -> {
            short lastCellNum = row.getLastCellNum();
            Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);

            if (item.length() > 10000) {
                logger.info("セル(row={}, column={})に出力する文字数が10,000文字を超えています。全ての文字は出力されません。", cell.getRowIndex(), cell.getColumnIndex());
                cell.setCellValue(item.substring(0, 10000) + "...(省略されました）");
            } else {
                cell.setCellValue(item);
            }
        });
    }
}
