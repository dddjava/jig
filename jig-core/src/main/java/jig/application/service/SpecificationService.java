package jig.application.service;

import jig.domain.model.specification.SpecificationReader;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import org.springframework.stereotype.Service;

@Service
public class SpecificationService {

    final SpecificationReader specificationReader;

    public SpecificationService(SpecificationReader specificationReader) {
        this.specificationReader = specificationReader;
    }

    public Specifications specification(SpecificationSources specificationSources) {
        if (specificationSources.notFound()) {
            throw new RuntimeException("解析対象のクラスが存在しないため処理を中断します。");
        }

        return specificationReader.readFrom(specificationSources);
    }
}
