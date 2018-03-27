package jig.application.service;

import jig.domain.model.datasource.SqlReader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.asm.ModelReader;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final ModelReader modelReader;
    final SqlReader sqlReader;
    final JapaneseReader japaneseReader;

    public AnalyzeService(ModelReader modelReader,
                          SqlReader sqlReader,
                          JapaneseReader japaneseReader) {
        this.modelReader = modelReader;
        this.sqlReader = sqlReader;
        this.japaneseReader = japaneseReader;
    }

    public void analyze(ProjectLocation projectLocation) {
        modelReader.readFrom(projectLocation);
        sqlReader.readFrom(projectLocation);

        readJavadoc(projectLocation);
    }

    public void readJavadoc(ProjectLocation projectLocation) {
        japaneseReader.readFrom(projectLocation);
    }

}
