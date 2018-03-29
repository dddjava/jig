package jig.domain.model.project;

import jig.domain.model.specification.SpecificationSources;

public interface ModelReader {

    SpecificationSources getSpecificationSources(ProjectLocation rootPath);

    void readFrom(SpecificationSources specificationSources);
}
