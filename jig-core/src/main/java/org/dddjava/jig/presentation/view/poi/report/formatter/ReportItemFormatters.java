package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionField;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ReportItemMethod;
import org.dddjava.jig.presentation.view.report.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * 一覧出力項目のフォーマッター
 */
public class ReportItemFormatters {
    static Logger logger = LoggerFactory.getLogger(ReportItemFormatters.class);

    List<ReportItemFormatter> reportItemFormatters;

    public ReportItemFormatters(ConvertContext convertContext) {
        this.reportItemFormatters = Arrays.asList(
                new BooleanFormatter(convertContext),
                new CallerMethodsFormatter(convertContext),
                new MethodDeclarationFormatter(convertContext),
                new MethodDeclarationsFormatter(convertContext),
                new MethodFormatter(convertContext),
                new StringFormatter(convertContext),
                new TypeFormatter(convertContext),
                new TypeIdentifierFormatter(convertContext),
                new TypeIdentifiersFormatter(convertContext),
                new PackageIdentifierFormatter(convertContext),
                new UsingFieldsFormatter(convertContext),
                new ServiceMethodsFormatter(convertContext)
        );
    }

    String format(ReportItem reportItem, Object item) {
        for (ReportItemFormatter reportItemFormatter : reportItemFormatters) {
            if (reportItemFormatter.canFormat(item)) {
                return reportItemFormatter.format(reportItem, item);
            }
        }

        if (item instanceof CollectionField) {
            CollectionField collectionField = (CollectionField) item;
            if (reportItem == ReportItem.フィールドの型) {
                return collectionField.fieldType().asSimpleText();
            }
        }
        if (item instanceof BusinessRules) {
            BusinessRules businessRules = (BusinessRules) item;
            if (reportItem == ReportItem.クラス数) {
                return String.valueOf(businessRules.list().size());
            }
        }

        throw new IllegalArgumentException(reportItem.name());
    }

    public void apply(Row row, ReportItemMethod reportItemMethod, Object methodReturnValue) {
        String result = format(reportItemMethod.value(), methodReturnValue);

        short lastCellNum = row.getLastCellNum();
        Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);

        if (result.length() > 10000) {
            logger.info("セル(row={}, column={})に出力する文字数が10,000文字を超えています。全ての文字は出力されません。",
                    cell.getRowIndex(), cell.getColumnIndex());
            cell.setCellValue(result.substring(0, 10000) + "...(省略されました）");
        } else {
            cell.setCellValue(result);
        }
    }
}
