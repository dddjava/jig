package org.dddjava.jig.domain.model.sources.jigfactory;

/**
 * アーキテクチャ
 */
public interface Architecture {

    boolean isRepositoryImplementation(JigTypeBuilder jigTypeBuilder);

    boolean isBusinessRule(JigTypeBuilder jigTypeBuilder);
}
