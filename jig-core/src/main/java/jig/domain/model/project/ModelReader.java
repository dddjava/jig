package jig.domain.model.project;

import jig.domain.model.relation.Relations;

public interface ModelReader {
    void readFrom(ProjectLocation rootPath);

    Relations relationsOf(ProjectLocation location);
}
