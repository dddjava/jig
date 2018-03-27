package jig.infrastructure.plantuml;

import jig.domain.model.identifier.NameFormatter;
import jig.domain.model.identifier.PackageIdentifier;

public class PlantumlNameFormatter implements NameFormatter {

    private String nameReplacePattern = "";

    @Override
    public String format(PackageIdentifier identifier) {
        String value = identifier.value()
                .replaceFirst(nameReplacePattern, "")
                .replaceAll("\\.", "/");
        return value;
    }

    public void setNameShortenPattern(String pattern) {
        nameReplacePattern = pattern;
    }
}
