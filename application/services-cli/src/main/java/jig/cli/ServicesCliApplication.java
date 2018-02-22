package jig.cli;

import jig.application.service.AnalyzeService;
import jig.domain.model.dependency.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static java.util.stream.Collectors.joining;

@SpringBootApplication(scanBasePackages = "jig")
public class ServicesCliApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ServicesCliApplication.class, args);
    }

    @Autowired
    AnalyzeService service;

    @Override
    public void run(String... args) throws Exception {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./services.tsv"), StandardCharsets.UTF_8)) {

            Models models = service.toModels(new AnalysisCriteria(
                    new SearchPaths(
                            Collections.singletonList(Paths.get(""))),
                    new AnalysisClassesPattern(".*.application\\.service\\..+"),
                    new DependenciesPattern(".*\\.(application\\.service\\..+|domain\\.model\\..+Repository)"),
                    AnalysisTarget.CLASS));
            String format = models.format(
                    new ModelFormatter() {
                        @Override
                        public String header() {
                            return "";
                        }

                        @Override
                        public String format(Model model) {
                            return model.format(
                                    fullQualifiedName -> fullQualifiedName.value()
                                            + "\n" + model.dependency().list().stream()
                                            .map(m -> m.format(FullQualifiedName::value))
                                            .map(s -> "\t" + s)
                                            .collect(joining("\n"))
                            );
                        }

                        @Override
                        public String footer() {
                            return "";
                        }
                    }
            );

            writer.write(format);
        }
    }
}
