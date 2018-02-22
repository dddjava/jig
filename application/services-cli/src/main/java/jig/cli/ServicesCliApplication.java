package jig.cli;

import jig.application.service.AnalyzeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            writer.write("test");
        }
    }
}
