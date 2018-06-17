package org.dddjava.jig.presentation.view.poi.report.handler;

import org.dddjava.jig.domain.basic.ReportItem;
import org.dddjava.jig.domain.basic.UserNumber;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;

public class TypeIdentifiersHandler implements ItemHandler {

    ConvertContext convertContext;

    public TypeIdentifiersHandler(ConvertContext convertContext) {
        this.convertContext = convertContext;
    }

    @Override
    public boolean canHandle(Object obj) {
        return obj instanceof TypeIdentifiers;
    }

    @Override
    public String handle(ReportItem item, Object obj) {
        TypeIdentifiers typeIdentifiers = (TypeIdentifiers) obj;
        switch (item) {
            case 使用箇所数:
                // TODO 使用箇所をTypeIdentifiersで扱ってるのが微妙な感じ
                return new UserNumber(typeIdentifiers.list().size()).asText();
            case 使用箇所:
                // TODO 使用箇所をTypeIdentifiersで扱ってるのが微妙な感じ
                return typeIdentifiers.asSimpleText();
        }

        throw new IllegalArgumentException();
    }
}
