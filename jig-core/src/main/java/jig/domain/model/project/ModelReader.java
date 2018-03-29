package jig.domain.model.project;

import jig.domain.model.specification.SpecificationSources;

public interface ModelReader {
    void readFrom(ProjectLocation rootPath);

    void readFrom(SpecificationSources specificationSources);
}
