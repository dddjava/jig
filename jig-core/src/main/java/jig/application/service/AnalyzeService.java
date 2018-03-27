package jig.application.service;

import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.javaparser.PackageInfoReader;
import jig.infrastructure.mybatis.MyBatisSqlResolver;
import jig.domain.model.datasource.SqlPath;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

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

    public void analyze(Path path) {
        asmClassFileReader.execute(path);

        myBatisSqlResolver.resolve(new SqlPath(path));

        readJavadoc(path);
    }

    public void readJavadoc(Path path) {
        classCommentReader.execute(path);
        packageInfoReader.execute(path);
    }
}
