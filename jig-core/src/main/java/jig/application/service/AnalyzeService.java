package jig.application.service;

import jig.infrastructure.RecursiveFileVisitor;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.javaparser.PackageInfoReader;
import jig.infrastructure.mybatis.MyBatisSqlResolver;
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
        RecursiveFileVisitor classVisitor = new RecursiveFileVisitor(asmClassFileReader::execute);
        classVisitor.visitAllDirectories(path);

        myBatisSqlResolver.resolve(path);

        readJavadoc(path);
    }

    public void readJavadoc(Path path) {
        RecursiveFileVisitor commentVisitor = new RecursiveFileVisitor(classCommentReader::execute);
        commentVisitor.visitAllDirectories(path);

        RecursiveFileVisitor fileVisitor = new RecursiveFileVisitor(packageInfoReader::execute);
        fileVisitor.visitAllDirectories(path);
    }
}
