package jig.application.service;

import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.tag.TagRepository;
import jig.infrastructure.RecursiveFileVisitor;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.mybatis.MyBatisSqlResolver;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class AnalyzeService {

    AsmClassFileReader asmClassFileReader;
    ClassCommentReader classCommentReader;

    SqlRepository sqlRepository;
    TagRepository tagRepository;

    public AnalyzeService(AsmClassFileReader asmClassFileReader, ClassCommentReader classCommentReader, SqlRepository sqlRepository, TagRepository tagRepository) {
        this.asmClassFileReader = asmClassFileReader;
        this.classCommentReader = classCommentReader;
        this.sqlRepository = sqlRepository;
        this.tagRepository = tagRepository;
    }

    public void analyze(Path path) {
        RecursiveFileVisitor classVisitor = new RecursiveFileVisitor(asmClassFileReader::execute);
        classVisitor.visitAllDirectories(path);

        RecursiveFileVisitor commentVisitor = new RecursiveFileVisitor(classCommentReader::execute);
        commentVisitor.visitAllDirectories(path);

        MyBatisSqlResolver myBatisSqlResolver = new MyBatisSqlResolver(sqlRepository, tagRepository);
        myBatisSqlResolver.resolve(path);
    }
}
