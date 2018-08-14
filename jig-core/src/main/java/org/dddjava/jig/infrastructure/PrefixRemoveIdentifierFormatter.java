package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifierFormatter;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.infrastructure.configuration.OutputOmitPrefix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrefixRemoveIdentifierFormatter implements TypeIdentifierFormatter, PackageIdentifierFormatter {

    private final OutputOmitPrefix prefixPattern;

    @Autowired
    public PrefixRemoveIdentifierFormatter(@Value("${output.omit.prefix:}") String prefixPattern) {
        this(new OutputOmitPrefix(prefixPattern));
    }

    public PrefixRemoveIdentifierFormatter(OutputOmitPrefix prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    @Override
    public String format(String fullQualifiedName) {
        return prefixPattern.format(fullQualifiedName);
    }
}
