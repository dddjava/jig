package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.architectures.ArchitectureComponent;

/**
 * アーキテクチャ
 */
public interface Architecture {

    boolean isService(TypeFact typeFact);

    boolean isRepositoryImplementation(TypeFact typeFact);

    boolean isController(TypeFact typeFact);

    boolean isBusinessRule(TypeFact typeFact);

    ArchitectureComponent architectureComponent(TypeFact typeFact);
}
