package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigdocumenter.collections.CollectionField;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
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
}
