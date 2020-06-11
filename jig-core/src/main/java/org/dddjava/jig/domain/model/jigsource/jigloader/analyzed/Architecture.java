package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

/**
 * アーキテクチャ
 */
public interface Architecture {

    boolean isService(TypeFact typeFact);

    boolean isDataSource(TypeFact typeFact);

    boolean isController(TypeFact typeFact);

    boolean isBusinessRule(TypeFact typeFact);
}
