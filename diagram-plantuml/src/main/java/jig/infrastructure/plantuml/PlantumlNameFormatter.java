package jig.infrastructure.plantuml;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.NameFormatter;

public class PlantumlNameFormatter implements NameFormatter {

    private String nameReplacePattern = "";

    @Override
    public String format(TypeIdentifier fullQualifiedTypeIdentifier) {
        String value = fullQualifiedTypeIdentifier.value()
                .replaceFirst(nameReplacePattern, "")
                .replaceAll("\\.", "/");
        return value;
    }

    public void setNameShortenPattern(String pattern) {
        nameReplacePattern = pattern;
    }
}
