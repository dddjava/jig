package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
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
            case クラス名:
                return convertContext.typeIdentifierFormatter.format(typeIdentifier.fullQualifiedName());
            case クラス和名:
                return convertContext.glossaryService.japaneseNameFrom(typeIdentifier).summarySentence();
            case 単純クラス名:
                return typeIdentifier.asSimpleText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
