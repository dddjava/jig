package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifierFormatter;

public class ConvertContext {
    public AliasService aliasService;
    public TypeIdentifierFormatter typeIdentifierFormatter;

    public ConvertContext(AliasService aliasService, TypeIdentifierFormatter typeIdentifierFormatter) {
        this.aliasService = aliasService;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }
}
