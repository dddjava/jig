package jig.cli;

import jig.domain.model.usage.ModelTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

@SpringBootApplication(scanBasePackages = "jig")
public class ServicesCliApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ServicesCliApplication.class, args);
    }

    @Autowired
    ModelTypeRepository repository;

    @Value("${output.file.delimiter}")
    String delimiter;

    @Value("${output.file.line-separator}")
    String lineSeparator;

    @Override
    public void run(String... args) throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("./services.tsv"), StandardCharsets.UTF_8)) {
            repository.findAll().list().forEach(serviceClass -> {
                serviceClass.methods().list().forEach(serviceMethod -> {
                    try {
                        // クラス名
                        writer.write(serviceClass.name().value());
                        writer.write(delimiter);
                        // 和名
                        writer.write(serviceClass.japaneseName().value());
                        writer.write(delimiter);
                        // フィールド型（列挙）
                        writer.write(serviceClass.dependents().list().stream().map(Class::getSimpleName).collect(joining(",")));
                        writer.write(delimiter);
                        // メソッド名
                        writer.write(serviceMethod.name());
                        writer.write(delimiter);
                        // メソッド型
                        writer.write(serviceMethod.returnType().getSimpleName());
                        writer.write(delimiter);
                        // メソッドパラメータ型（列挙）
                        writer.write(Arrays.stream(serviceMethod.parameters()).map(Class::getSimpleName).collect(joining(",")));

                        writer.write(lineSeparator);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            });
        }
    }
}

