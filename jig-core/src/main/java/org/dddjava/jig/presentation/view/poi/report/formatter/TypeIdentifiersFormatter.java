package org.dddjava.jig.presentation.view.poi.report.formatter;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.UserNumber;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

class TypeIdentifiersFormatter implements ReportItemFormatter {

    ConvertContext convertContext;

    TypeIdentifiersFormatter(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canFormat(Object item) {
        return item instanceof TypeIdentifiers;
    }

    @Override
    public String format(ReportItem itemCategory, Object item) {
        TypeIdentifiers typeIdentifiers = (TypeIdentifiers) item;
        switch (itemCategory) {
            case 使用箇所数:
                // TODO 使用箇所をTypeIdentifiersで扱ってるのが微妙な感じ
                return new UserNumber(typeIdentifiers.list().size()).asText();
            case 使用箇所:
                // TODO 使用箇所をTypeIdentifiersで扱ってるのが微妙な感じ
                return typeIdentifiers.asSimpleText();
        }

        throw new IllegalArgumentException(itemCategory.name());
    }
}
