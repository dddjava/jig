package jig.application.service;

import jig.domain.model.datasource.SqlReader;
import jig.domain.model.japanasename.JapaneseReader;
import jig.domain.model.project.ModelReader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.JigPaths;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final ModelReader modelReader;
    final SqlReader sqlReader;
    final JapaneseReader japaneseReader;
    final JigPaths jigPaths;

    public AnalyzeService(ModelReader modelReader,
                          SqlReader sqlReader,
                          JapaneseReader japaneseReader,
                          JigPaths jigPaths) {
        this.modelReader = modelReader;
        this.sqlReader = sqlReader;
        this.japaneseReader = japaneseReader;
        this.jigPaths = jigPaths;
    }

    public void analyze(ProjectLocation projectLocation) {
        modelReader.readFrom(jigPaths.getSpecificationSources(projectLocation));
        sqlReader.readFrom(projectLocation);

        readJavadoc(projectLocation);
    }

    public void readJavadoc(ProjectLocation projectLocation) {
        japaneseReader.readFrom(projectLocation);
    }

    public void analyzeModelOnly(ProjectLocation location) {
        modelReader.readFrom(jigPaths.getSpecificationSources(location));
    }
}
