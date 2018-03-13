package jig.classlist;

import jig.infrastructure.reflection.ModelTypeClassLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClassListApplication.class, args);

        context.getBean(ModelTypeClassLoader.class).load();

        Path path = Paths.get(context.getEnvironment().getProperty("output.list.name", "class-list.tsv"));

        if (path.toString().endsWith(".tsv")) {
            context.getBean(TsvWriter.class).writeTo(path);
        } else if (path.toString().endsWith(".xlsx")) {
            context.getBean(ExcelWriter.class).writeTo(path);
        }
    }
}

