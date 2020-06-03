package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class TypeIdentifierFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    TypeIdentifierFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof TypeIdentifier;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        TypeIdentifier typeIdentifier = (TypeIdentifier) item;
        switch (itemCategory) {
            case パッケージ名:
                return typeIdentifier.packageIdentifier().asText();
            case クラス名:
            case 単純クラス名:
                return typeIdentifier.asSimpleText();
            case クラス別名:
                return convertContext.aliasService.typeAliasOf(typeIdentifier).asText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
