package jig.infrastructure.plantuml;

import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.NameFormatter;

public class PlantumlNameFormatter implements NameFormatter {

    private String nameReplacePattern = "";

    @Override
    public String format(Identifier fullQualifiedIdentifier) {
        String value = fullQualifiedIdentifier.value()
                .replaceFirst(nameReplacePattern, "")
                .replaceAll("\\.", "/");
        return value;
    }

    public void setNameShortenPattern(String pattern) {
        nameReplacePattern = pattern;
    }
}
