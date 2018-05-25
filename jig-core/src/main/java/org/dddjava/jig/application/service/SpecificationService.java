package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.ImplementationSources;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.springframework.stereotype.Service;

/**
 * 仕様サービス
 */
@Service
public class SpecificationService {

    final ImplementationFactory implementationFactory;

    public SpecificationService(ImplementationFactory implementationFactory) {
        this.implementationFactory = implementationFactory;
    }

    /**
     * ソースから実装を読み取る
     */
    public Implementations readImplementation(ImplementationSources implementationSources) {
        if (implementationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return implementationFactory.readFrom(implementationSources);
    }
}
