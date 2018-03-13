package jig.classlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClassListApplication.class, args);
        Path path = Paths.get(context.getEnvironment().getProperty("output.list.name", "class-list.tsv"));
        context.getBean(TsvWriter.class).writeTo(path);
    }
}

