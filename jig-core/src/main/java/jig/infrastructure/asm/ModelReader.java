package jig.infrastructure.asm;

import jig.domain.model.project.ProjectLocation;

public interface ModelReader {
    void readFrom(ProjectLocation rootPath);
}
