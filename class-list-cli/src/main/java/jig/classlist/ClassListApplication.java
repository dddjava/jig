package jig.classlist;

import jig.application.service.report.ReportService;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.report.Report;
import jig.domain.model.tag.Tag;
import jig.domain.model.tag.TagRepository;
import jig.infrastructure.RecursiveFileVisitor;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.ClassCommentReader;
import jig.infrastructure.mybatis.MyBatisSqlResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClassListApplication.class, args);

        context.getBean(ClassListApplication.class).output();
    }

    @Value("${output.list.name}")
    String outputPath;
    @Value("${output.list.type}")
    String listType;

    @Value("${project.path}")
    String projectPath;

    @Autowired
    AsmClassFileReader asmClassFileReader;
    @Autowired
    ReportService reportService;
    @Autowired
    ClassCommentReader classCommentReader;

    public void output() {
        Path path = Paths.get(projectPath);

        RecursiveFileVisitor classVisitor = new RecursiveFileVisitor(asmClassFileReader::execute);
        classVisitor.visitAllDirectories(path);

        RecursiveFileVisitor commentVisitor = new RecursiveFileVisitor(classCommentReader::execute);
        commentVisitor.visitAllDirectories(path);

        MyBatisSqlResolver myBatisSqlResolver = new MyBatisSqlResolver(sqlRepository, tagRepository);
        myBatisSqlResolver.resolve(path);

        Tag tag = Tag.valueOf(listType.toUpperCase());

        Report report = reportService.getReport(tag);

        ReportFormat.from(outputPath)
                .writer()
                .writeTo(report, Paths.get(outputPath));
    }

    @Autowired
    SqlRepository sqlRepository;
    @Autowired
    TagRepository tagRepository;
}

