package jig.cli;

import jig.analizer.javaparser.PackageInfoParser;
import jig.cli.infrastructure.usage.ModelTypeFactory;
import jig.domain.model.dependency.FullQualifiedName;
import jig.domain.model.dependency.JapaneseNameRepository;
import jig.domain.model.usage.DependentTypes;
import jig.domain.model.usage.ModelMethods;
import jig.domain.model.usage.ModelType;
import jig.domain.model.usage.ModelTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Value("${output.list.column.delimiter}")
    String delimiter;
    @Value("${output.list.name}")
    String outputFileName;

    @Override
    public void run(String... args) throws Exception {

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFileName), StandardCharsets.UTF_8)) {
            writer.write("クラス名");
            writer.write(delimiter);
            writer.write("クラス和名");
            writer.write(delimiter);
            writer.write("メソッド名");
            writer.write(delimiter);
            writer.write("メソッド戻り値の型");
            writer.write(delimiter);
            writer.write("メソッド引数型");
            writer.write(delimiter);
            writer.write("保持しているフィールドの型");
            writer.newLine();

            repository.findAll().list().forEach(modelType -> {
                modelType.methods().list().forEach(serviceMethod -> {
                    try {
                        // クラス名
                        writer.write(modelType.name().value());
                        writer.write(delimiter);
                        // 和名
                        writer.write(modelType.japaneseName().value());
                        writer.write(delimiter);
                        // メソッド名
                        writer.write(serviceMethod.name());
                        writer.write(delimiter);
                        // メソッド型
                        writer.write(serviceMethod.returnType().getSimpleName());
                        writer.write(delimiter);
                        // メソッドパラメータ型（列挙）
                        writer.write(Arrays.stream(serviceMethod.parameters()).map(Class::getSimpleName).collect(joining(",")));
                        // フィールド型（列挙）
                        writer.write(modelType.dependents().list().stream().map(Class::getSimpleName).collect(joining(",")));
                        writer.write(delimiter);

                        writer.newLine();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            });
        }
    }

    @Bean
    JapaneseNameRepository japaneseNameRepository(@Value("${target.source}") String sourcePath) {
        PackageInfoParser packageInfoParser = new PackageInfoParser(Paths.get(sourcePath));
        return packageInfoParser.parseClass();
    }

    @ConditionalOnProperty(name = "output.list.type", havingValue = "service")
    @Bean
    ModelTypeFactory serviceMethod(JapaneseNameRepository japaneseNames) {
        return new ModelTypeFactory() {
            @Override
            public boolean isTargetClass(Path path) {
                return path.toString().endsWith("Service.class");
            }

            @Override
            public ModelType toModelType(Class<?> clz) {
                FullQualifiedName fullQualifiedName = new FullQualifiedName(clz.getCanonicalName());
                return new ModelType(
                        fullQualifiedName,
                        japaneseNames.get(fullQualifiedName),
                        ModelMethods.from(clz),
                        DependentTypes.from(clz));
            }
        };
    }

    @ConditionalOnProperty(name = "output.list.type", havingValue = "repository")
    @Bean
    ModelTypeFactory serviceRepository(JapaneseNameRepository japaneseNames) {
        return new ModelTypeFactory() {
            @Override
            public boolean isTargetClass(Path path) {
                return path.toString().endsWith("Repository.class");
            }

            @Override
            public ModelType toModelType(Class<?> clz) {
                FullQualifiedName fullQualifiedName = new FullQualifiedName(clz.getCanonicalName());
                return new ModelType(
                        fullQualifiedName,
                        japaneseNames.get(fullQualifiedName),
                        ModelMethods.from(clz),
                        DependentTypes.empty());
            }
        };
    }
}

