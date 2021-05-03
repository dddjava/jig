package org.dddjava.jig.domain.model.sources.jigfactory;

/**
 * アーキテクチャ
 */
public interface Architecture {

    boolean isRepositoryImplementation(TypeFact typeFact);

    boolean isBusinessRule(TypeFact typeFact);
}
