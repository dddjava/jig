package jig.domain.model.datasource;

import jig.domain.model.project.ProjectLocation;

public interface SqlLoader {

    void loadFrom(ProjectLocation projectLocation);
}
