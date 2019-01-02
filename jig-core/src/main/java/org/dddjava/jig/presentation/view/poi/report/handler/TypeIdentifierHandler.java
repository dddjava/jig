package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.model.implementation.declaration.type.TypeIdentifier;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.report.ReportItem;

public class TypeIdentifierHandler implements ItemHandler {

    ConvertContext convertContext;

    public TypeIdentifierHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof TypeIdentifier;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        TypeIdentifier typeIdentifier = (TypeIdentifier) obj;
        switch (item) {
            case クラス名:
                return convertContext.typeIdentifierFormatter.format(typeIdentifier.fullQualifiedName());
            case クラス和名:
                return convertContext.glossaryService.japaneseNameFrom(typeIdentifier).summarySentence();
            case 単純クラス名:
                return typeIdentifier.asSimpleText();
        }

        throw new IllegalArgumentException(item.name());
    }
}
