package org.dddjava.jig.domain.basic;

import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;

public class ReportContext {

    private Object value;
    private GlossaryService glossaryService;
    private TypeIdentifierFormatter typeIdentifierFormatter;

    public ReportContext(Object value, GlossaryService glossaryService, TypeIdentifierFormatter typeIdentifierFormatter) {
        this.value = value;
        this.glossaryService = glossaryService;
        this.typeIdentifierFormatter = typeIdentifierFormatter;
    }

    public <T> T value(Class<T> clz) {
        return clz.cast(value);
    }

    public String typeJapaneseName(TypeIdentifier typeIdentifier) {
        return glossaryService.japaneseNameFrom(typeIdentifier).summarySentence();
    }

    public String formatTypeIdentifier(TypeIdentifier typeIdentifier) {
        return typeIdentifierFormatter.format(typeIdentifier.fullQualifiedName());
    }

    public String methodJapaneseName(MethodIdentifier methodIdentifier) {
        return glossaryService.japaneseNameFrom(methodIdentifier).summarySentence();
    }
}
