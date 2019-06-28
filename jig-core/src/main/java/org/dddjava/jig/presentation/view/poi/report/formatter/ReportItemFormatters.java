package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.collections.CollectionField;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

import java.util.Arrays;
import java.util.List;

/**
 * 一覧出力項目のフォーマッター
 */
public class ReportItemFormatters {

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

    public String format(ReportItem reportItem, Object item) {
        for (ReportItemFormatter reportItemFormatter : reportItemFormatters) {
            if (reportItemFormatter.canFormat(item)) {
                return reportItemFormatter.format(reportItem, item);
            }
        }

        if (item instanceof CollectionField) {
            if (reportItem == ReportItem.フィールドの型) {
                return ((CollectionField)item).fieldType().asSimpleText();
            }
        }

        throw new IllegalArgumentException(reportItem.name());
    }
}
