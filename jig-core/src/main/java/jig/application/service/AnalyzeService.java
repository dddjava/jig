package jig.application.service;

import jig.domain.model.datasource.SqlLoader;
import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.javaparser.PackageInfoReader;
import jig.infrastructure.mybatis.MyBatisSqlLoader;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    AsmClassFileReader asmClassFileReader;
    ClassCommentReader classCommentReader;
    PackageInfoReader packageInfoReader;
    SqlLoader sqlLoader;

    public AnalyzeService(AsmClassFileReader asmClassFileReader,
                          ClassCommentReader classCommentReader,
                          PackageInfoReader packageInfoReader,
                          MyBatisSqlLoader sqlLoader) {
        this.asmClassFileReader = asmClassFileReader;
        this.classCommentReader = classCommentReader;
        this.packageInfoReader = packageInfoReader;
        this.sqlLoader = sqlLoader;
    }

    public void analyze(ProjectLocation projectLocation) {
        asmClassFileReader.execute(projectLocation);

        sqlLoader.loadFrom(projectLocation);

        readJavadoc(projectLocation);
    }

    public void readJavadoc(ProjectLocation projectLocation) {
        classCommentReader.execute(projectLocation.getValue());
        packageInfoReader.execute(projectLocation.getValue());
    }
}
