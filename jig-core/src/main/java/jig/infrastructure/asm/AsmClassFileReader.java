package jig.infrastructure.asm;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.identifier.PackageIdentifiers;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.project.ModelReader;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.relation.RelationType;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.domain.model.relation.dependency.PackageDependency;
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
    public PackageDependencies dependenciesIn(ProjectLocation location) {
        readFrom(location);

        Identifiers modelIdentifiers = characteristicRepository.find(Characteristic.MODEL);
        List<PackageDependency> list =
                modelIdentifiers.list().stream()
                        .flatMap(identifier -> {
                            PackageIdentifier packageIdentifier = identifier.packageIdentifier();
                            return relationRepository.findAllUsage(identifier)
                                    .filter(usage -> characteristicRepository.has(usage, Characteristic.MODEL))
                                    .list().stream()
                                    .map(TypeIdentifier::packageIdentifier)
                                    .filter(usagePackage -> !packageIdentifier.equals(usagePackage))
                                    .map(usagePackage -> new PackageDependency(usagePackage, packageIdentifier));
                        })
                        .distinct()
                        .collect(Collectors.toList());

        PackageIdentifiers allPackages = new PackageIdentifiers(
                modelIdentifiers.list().stream()
                        .map(TypeIdentifier::packageIdentifier)
                        .collect(Collectors.toList()));

        return new PackageDependencies(list, allPackages);
    }

    private void executeInternal(Path path) {
        LOGGER.debug("class取り込み: {}", path);
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