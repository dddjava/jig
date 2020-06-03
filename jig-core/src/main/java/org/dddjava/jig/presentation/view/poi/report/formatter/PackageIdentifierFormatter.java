package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class PackageIdentifierFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    PackageIdentifierFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof PackageIdentifier;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        PackageIdentifier packageIdentifier = (PackageIdentifier) item;
        switch (itemCategory) {
            case パッケージ名:
                return packageIdentifier.asText();
            case パッケージ別名:
                return convertContext.aliasService.packageAliasOf(packageIdentifier).asText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
