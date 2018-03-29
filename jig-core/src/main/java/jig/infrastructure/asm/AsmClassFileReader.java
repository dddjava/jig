package jig.infrastructure.asm;

import jig.domain.model.project.ModelReader;
import jig.domain.model.specification.Specification;
import jig.domain.model.specification.SpecificationSource;
import jig.domain.model.specification.SpecificationSources;
import jig.domain.model.specification.Specifications;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AsmClassFileReader implements ModelReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsmClassFileReader.class);

    @Override
    public Specifications readFrom(SpecificationSources specificationSources) {
        List<Specification> list = new ArrayList<>();
        for (SpecificationSource source : specificationSources.list()) {
            Specification specification = readSpecification(source);
            list.add(specification);
        }
        return new Specifications(list);
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