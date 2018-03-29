package jig.domain.model.project;

import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;

public interface ModelReader {

    Specifications readFrom(SpecificationSources specificationSources);
}
