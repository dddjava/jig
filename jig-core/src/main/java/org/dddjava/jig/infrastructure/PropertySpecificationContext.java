package org.dddjava.jig.infrastructure;

import org.dddjava.jig.domain.model.specification.Specification;
import org.dddjava.jig.domain.model.specification.SpecificationContext;
import org.dddjava.jig.domain.model.specification.Specification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertySpecificationContext implements SpecificationContext {

    @Value("${jig.model.pattern:.+\\.domain\\.model\\..+}")
    String modelPattern = ".+\\.domain\\.model\\..+";

    @Value("${jig.repository.pattern:.+Repository}")
    String repositoryPattern = ".+Repository";

    @Override
    public boolean isModel(Specification specification) {
        return specification.typeIdentifier().fullQualifiedName().matches(modelPattern);
    }

    @Override
    public boolean isRepository(Specification specification) {
        return specification.typeIdentifier().fullQualifiedName().matches(repositoryPattern);
    }
}
