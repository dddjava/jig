package org.dddjava.jig.presentation.view.poi.report;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;

public class ConvertContext {
    public GlossaryService glossaryService;
    public TypeIdentifierFormatter typeIdentifierFormatter;

    public ConvertContext(GlossaryService glossaryService, TypeIdentifierFormatter typeIdentifierFormatter) {
        this.glossaryService = glossaryService;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }
}
