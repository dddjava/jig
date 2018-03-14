package jig.classlist;

import jig.infrastructure.asm.AsmExecutor;
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

        String classes = context.getEnvironment().getProperty("target.class");
        if (classes == null) throw new IllegalArgumentException();
        Path[] paths = Arrays.stream(classes.split(":"))
                .map(Paths::get)
                .toArray(Path[]::new);
        context.getBean(AsmExecutor.class).load(paths);

        Path path = Paths.get(context.getEnvironment().getProperty("output.list.name", "class-list.tsv"));

        if (path.toString().endsWith(".tsv")) {
            context.getBean(TsvWriter.class).writeTo(path);
        } else if (path.toString().endsWith(".xlsx")) {
            context.getBean(ExcelWriter.class).writeTo(path);
        }
    }
}

