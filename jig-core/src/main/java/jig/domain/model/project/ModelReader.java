package jig.domain.model.project;

import jig.domain.model.relation.dependency.PackageDependencies;

public interface ModelReader {
    void readFrom(ProjectLocation rootPath);

    PackageDependencies packageDependencies();
}
