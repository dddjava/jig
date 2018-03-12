package jig.infrastructure.plantuml;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.NameFormatter;

public class PlantumlNameFormatter implements NameFormatter {

    private String nameReplacePattern = "";

    @Override
    public String format(Name fullQualifiedName) {
        String value = fullQualifiedName.value()
                .replaceFirst(nameReplacePattern, "")
                .replaceAll("\\.", "/");
        return value;
    }

    public void setNameShortenPattern(String pattern) {
        nameReplacePattern = pattern;
    }
}
