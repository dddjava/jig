package jig.application.service;

import jig.domain.model.project.ProjectLocation;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.javaparser.PackageInfoReader;
import jig.infrastructure.mybatis.MyBatisSqlResolver;
import org.springframework.stereotype.Service;

@Service
public class AnalyzeService {

    AsmClassFileReader asmClassFileReader;
    ClassCommentReader classCommentReader;
    PackageInfoReader packageInfoReader;
    MyBatisSqlResolver myBatisSqlResolver;

    public AnalyzeService(AsmClassFileReader asmClassFileReader,
                          ClassCommentReader classCommentReader,
                          PackageInfoReader packageInfoReader,
                          MyBatisSqlResolver myBatisSqlResolver) {
        this.asmClassFileReader = asmClassFileReader;
        this.classCommentReader = classCommentReader;
        this.packageInfoReader = packageInfoReader;
        this.myBatisSqlResolver = myBatisSqlResolver;
    }

    public void analyze(ProjectLocation projectLocation) {
        asmClassFileReader.execute(projectLocation);

        myBatisSqlResolver.resolve(projectLocation);

        readJavadoc(projectLocation);
    }

    public void readJavadoc(ProjectLocation projectLocation) {
        classCommentReader.execute(projectLocation.getValue());
        packageInfoReader.execute(projectLocation.getValue());
    }
}
