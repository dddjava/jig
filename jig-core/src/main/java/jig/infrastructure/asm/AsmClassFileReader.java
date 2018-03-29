package jig.infrastructure.asm;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.project.ModelReader;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.specification.Specification;
import jig.domain.model.specification.SpecificationSource;
import jig.domain.model.specification.SpecificationSources;
import jig.infrastructure.JigPaths;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

@Component
public class AsmClassFileReader implements ModelReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsmClassFileReader.class);

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;
    private final JigPaths jigPaths;

    public AsmClassFileReader(CharacteristicRepository characteristicRepository, RelationRepository relationRepository, JigPaths jigPaths) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.jigPaths = jigPaths;
    }

    @Override
    public void readFrom(ProjectLocation rootPath) {
        ArrayList<SpecificationSource> sources = new ArrayList<>();
        try {
            for (Path path : jigPaths.extractClassPath(rootPath.getValue())) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (jigPaths.isClassFile(file)) {
                            SpecificationSource specificationSource = new SpecificationSource(file);
                            sources.add(specificationSource);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        SpecificationSources specificationSources = new SpecificationSources(sources);


        for (SpecificationSource source : specificationSources.list()) {
            Specification specification = readSpecification(source);
            register(specification);
        }
    }

    private void register(Specification specification) {
        Characteristic.register(characteristicRepository, specification);
        RelationType.register(relationRepository, specification);
    }

    private Specification readSpecification(SpecificationSource specificationSource) {
        LOGGER.debug("class取り込み: {}", specificationSource.getPath());
        try (InputStream inputStream = Files.newInputStream(specificationSource.getPath())) {
            SpecificationReadingVisitor visitor = new SpecificationReadingVisitor();
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            return visitor.specification();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}