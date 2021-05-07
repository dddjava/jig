package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.jigdocument.stationery.PackageIdentifierFormatter;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;

public class PrefixRemoveIdentifierFormatter implements TypeIdentifierFormatter, PackageIdentifierFormatter {

    private final OutputOmitPrefix prefixPattern;

    public PrefixRemoveIdentifierFormatter(OutputOmitPrefix prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    @Override
    public String format(String fullQualifiedName) {
        return prefixPattern.format(fullQualifiedName);
    }
}
