package org.dddjava.jig.domain.model.implementation;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ImplementationFactory {

    Implementations readFrom(ImplementationSources implementationSources);
}
