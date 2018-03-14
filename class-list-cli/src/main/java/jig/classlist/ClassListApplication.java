package jig.classlist;

import jig.classlist.methodlist.MethodListService;
import jig.classlist.methodlist.MethodListType;
import jig.classlist.methodlist.MethodReportFactory;
import jig.domain.model.tag.Tag;
import jig.infrastructure.asm.AsmExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClassListApplication.class, args);

        context.getBean(ClassListApplication.class).output();
    }

    @Value("${target.class}")
    String targetClasses;
    @Value("${output.list.name}")
    String outputPath;
    @Value("${output.list.type}")
    String listType;

    @Autowired
    AsmExecutor asmExecutor;
    @Autowired
    MethodListService methodListService;

    public void output() {
        Path[] paths = Arrays.stream(targetClasses.split(":"))
                .map(Paths::get)
                .toArray(Path[]::new);
        asmExecutor.load(paths);

        Tag tag = Tag.valueOf(listType.toUpperCase());

        ReportFactory<?> factory = getFactory(tag);

        Path path = Paths.get(outputPath);
        if (path.toString().endsWith(".tsv")) {
            new TsvWriter().writeTo(factory, path);
        } else if (path.toString().endsWith(".xlsx")) {
            new ExcelWriter().writeTo(factory, path);
        }
    }

    private ReportFactory<?> getFactory(Tag tag) {
        if (tag == Tag.SERVICE || tag == Tag.REPOSITORY) {
            return new MethodReportFactory(MethodListType.valueOf(tag.name()), methodListService.list(tag));
        }

        throw new IllegalArgumentException(tag.name());
    }
}

