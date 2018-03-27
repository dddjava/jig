package jig.application.service;

import jig.domain.model.datasource.SqlLoader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.asm.ModelReader;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final ModelReader modelReader;
    final SqlLoader sqlLoader;
    final JapaneseReader japaneseReader;

    public AnalyzeService(ModelReader modelReader,
                          SqlLoader sqlLoader,
                          JapaneseReader japaneseReader) {
        this.modelReader = modelReader;
        this.sqlLoader = sqlLoader;
        this.japaneseReader = japaneseReader;
    }

    public void analyze(ProjectLocation projectLocation) {
        modelReader.readFrom(projectLocation);
        sqlLoader.loadFrom(projectLocation);

        readJavadoc(projectLocation);
    }

    public void readJavadoc(ProjectLocation projectLocation) {
        japaneseReader.readFrom(projectLocation);
    }

}
