package jig.application.service;

import jig.domain.model.datasource.SqlLoader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.mybatis.MyBatisSqlLoader;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    final AsmClassFileReader asmClassFileReader;
    final SqlLoader sqlLoader;
    final JapaneseReader japaneseReader;

    public AnalyzeService(AsmClassFileReader asmClassFileReader,
                          MyBatisSqlLoader sqlLoader,
                          JapaneseReader japaneseReader) {
        this.asmClassFileReader = asmClassFileReader;
        this.sqlLoader = sqlLoader;
        this.japaneseReader = japaneseReader;
    }

    public void analyze(ProjectLocation projectLocation) {
        asmClassFileReader.execute(projectLocation);

        sqlLoader.loadFrom(projectLocation);

        readJavadoc(projectLocation);
    }

    public void readJavadoc(ProjectLocation projectLocation) {
        japaneseReader.readFrom(projectLocation);
    }

}
