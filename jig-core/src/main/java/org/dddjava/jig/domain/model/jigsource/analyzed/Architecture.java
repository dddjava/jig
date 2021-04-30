package org.dddjava.jig.domain.model.jigsource.analyzed;

import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureComponent;

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
