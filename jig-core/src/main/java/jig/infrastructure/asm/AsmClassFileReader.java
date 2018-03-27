package jig.infrastructure.asm;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.project.ModelReader;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.Relation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.Relations;
import jig.domain.model.specification.Specification;
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
import java.util.List;
import java.util.stream.Collectors;

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
        try {
            for (Path path : jigPaths.extractClassPath(rootPath.getValue())) {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (jigPaths.isClassFile(file)) executeInternal(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Relations relationsOf(ProjectLocation location) {
        readFrom(location);

        Identifiers modelIdentifiers = characteristicRepository.find(Characteristic.MODEL);
        List<Relation> list =
                modelIdentifiers.list().stream()
                        .flatMap(identifier -> {
                            Identifier packageIdentifier = identifier.asPackage();
                            return relationRepository.findAllUsage(identifier)
                                    .filter(usage -> characteristicRepository.has(usage, Characteristic.MODEL))
                                    .list().stream()
                                    .map(Identifier::asPackage)
                                    .filter(usagePackage -> !packageIdentifier.equals(usagePackage))
                                    .map(usagePackage -> new Relation(usagePackage, packageIdentifier, RelationType.DEPENDENCY));
                        })
                        .distinct()
                        .collect(Collectors.toList());

        LOGGER.info("関連の数: {}", list.size());

        return new Relations(list);
    }

    private void executeInternal(Path path) {
        LOGGER.debug("parsing: {}", path);
        try (InputStream inputStream = Files.newInputStream(path)) {
            SpecificationReadingVisitor classVisitor = new SpecificationReadingVisitor();
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);

            Specification specification = classVisitor.specification();
            Characteristic.register(characteristicRepository, specification);
            RelationType.register(relationRepository, specification);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}