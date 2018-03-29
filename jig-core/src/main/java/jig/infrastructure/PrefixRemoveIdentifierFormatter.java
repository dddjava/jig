package jig.infrastructure;

import jig.domain.model.identifier.PackageIdentifierFormatter;
import jig.domain.model.identifier.TypeIdentifierFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrefixRemoveIdentifierFormatter implements TypeIdentifierFormatter, PackageIdentifierFormatter {

    private final String prefixPattern;

    public PrefixRemoveIdentifierFormatter(@Value("${output.omit.prefix:}") String prefixPattern) {
        this.prefixPattern = prefixPattern;
    }

    @Override
    public String format(String fullQualifiedName) {
        return fullQualifiedName.replaceFirst(prefixPattern, "");
    }
}
