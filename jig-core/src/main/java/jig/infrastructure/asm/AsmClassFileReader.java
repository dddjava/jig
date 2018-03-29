package jig.infrastructure.asm;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.project.ModelReader;
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
import java.nio.file.Files;

@Component
public class AsmClassFileReader implements ModelReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsmClassFileReader.class);

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;

    public AsmClassFileReader(CharacteristicRepository characteristicRepository, RelationRepository relationRepository, JigPaths jigPaths) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
    }

    @Override
    public void readFrom(SpecificationSources specificationSources) {
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