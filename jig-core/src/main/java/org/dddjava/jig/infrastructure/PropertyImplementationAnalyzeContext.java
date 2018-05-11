package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.implementation.Implementation;
import org.dddjava.jig.domain.model.implementation.ImplementationAnalyzeContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyImplementationAnalyzeContext implements ImplementationAnalyzeContext {

    @Value("${jig.model.pattern:.+\\.domain\\.model\\..+}")
    String modelPattern = ".+\\.domain\\.model\\..+";

    @Value("${jig.repository.pattern:.+Repository}")
    String repositoryPattern = ".+Repository";

    @Override
    public boolean isModel(Implementation implementation) {
        return implementation.typeIdentifier().fullQualifiedName().matches(modelPattern);
    }

    @Override
    public boolean isRepository(Implementation implementation) {
        return implementation.typeIdentifier().fullQualifiedName().matches(repositoryPattern);
    }
}
