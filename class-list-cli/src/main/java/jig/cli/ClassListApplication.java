package jig.cli;

import jig.cli.infrastructure.usage.ModelTypeFactory;
import jig.domain.model.list.ModelKind;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.tag.JapaneseNameDictionary;
import jig.domain.model.tag.JapaneseNameDictionaryLibrary;
import jig.domain.model.thing.Name;
import jig.domain.model.usage.ModelMethod;
import jig.domain.model.usage.ModelMethods;
import jig.domain.model.usage.ModelType;
import jig.domain.model.usage.ModelTypeRepository;
import jig.infrastructure.OnMemoryRelationRepository;
import jig.infrastructure.javaparser.ClassCommentLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.util.stream.Collectors.joining;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(ClassListApplication.class.getName());
    private static final String delimiter = "\t";

    public static void main(String[] args) {
        SpringApplication.run(ClassListApplication.class, args);
    }

    @Autowired
    ModelTypeRepository repository;

    @Value("${output.list.name}")
    String outputFileName;

    @Value("${output.list.type}")
    String modelKind;

    @Autowired
    RelationRepository relationRepository;

    @Bean
    RelationRepository relationRepository() {
        return new OnMemoryRelationRepository();
    }

    @Override
    public void run(String... args) throws Exception {
        ModelKind modelKind = ModelKind.valueOf(this.modelKind.toUpperCase());

        Path output = Paths.get(outputFileName);
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write(modelKind.headerLabel().stream().collect(joining(delimiter)));
            writer.newLine();

            for (ModelType modelType : repository.findAll().list()) {
                for (ModelMethod method : modelType.methods().list()) {
                    writer.write(modelKind.row(modelType, method, relationRepository).stream().collect(joining(delimiter)));
                    writer.newLine();
                }
            }
        }
        logger.info(output.toAbsolutePath() + "を出力しました。");
    }

    @Bean
    JapaneseNameDictionaryLibrary japaneseNameRepository(@Value("${target.source}") String sourcePath) {
        return new ClassCommentLibrary(Paths.get(sourcePath));
    }

    @ConditionalOnProperty(name = "output.list.type", havingValue = "service")
    @Bean
    ModelTypeFactory serviceMethod(JapaneseNameDictionaryLibrary library) {
        JapaneseNameDictionary japaneseNameDictionary = library.borrow();
        return new ModelTypeFactory() {
            @Override
            public boolean isTargetClass(Path path) {
                return path.toString().endsWith("Service.class");
            }

            @Override
            public ModelType toModelType(Class<?> clz) {
                Name name = new Name(clz);
                return new ModelType(
                        name,
                        japaneseNameDictionary.get(name),
                        ModelMethods.from(clz)
                );
            }
        };
    }

    @ConditionalOnProperty(name = "output.list.type", havingValue = "repository")
    @Bean
    ModelTypeFactory serviceRepository(JapaneseNameDictionaryLibrary library) {
        JapaneseNameDictionary japaneseNameDictionary = library.borrow();
        return new ModelTypeFactory() {
            @Override
            public boolean isTargetClass(Path path) {
                return path.toString().endsWith("Repository.class");
            }

            @Override
            public ModelType toModelType(Class<?> clz) {
                Name name = new Name(clz);
                return new ModelType(
                        name,
                        japaneseNameDictionary.get(name),
                        ModelMethods.from(clz)
                );
            }
        };
    }
}

