package jig.domain.model.datasource;

import jig.domain.model.project.ProjectLocation;

public interface SqlReader {

    void readFrom(ProjectLocation projectLocation);
}
