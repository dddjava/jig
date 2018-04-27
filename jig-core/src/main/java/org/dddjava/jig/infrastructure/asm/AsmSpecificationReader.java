package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.specification.*;
import org.dddjava.jig.domain.model.specification.SpecificationSource;
import org.dddjava.jig.domain.model.specification.SpecificationSources;
import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AsmSpecificationReader implements SpecificationReader {

    final SpecificationContext specificationContext;

    public AsmSpecificationReader(SpecificationContext specificationContext) {
        this.specificationContext = specificationContext;
    }

    @Override
    public Specifications readFrom(SpecificationSources specificationSources) {
        List<Specification> list = new ArrayList<>();
        for (SpecificationSource source : specificationSources.list()) {
            Specification specification = readSpecification(source);
            list.add(specification);
        }
        return new Specifications(list);
    }

    Specification readSpecification(SpecificationSource specificationSource) {
        try (InputStream inputStream = Files.newInputStream(specificationSource.getPath())) {
            SpecificationReadingVisitor visitor = new SpecificationReadingVisitor(specificationContext);
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            return visitor.specification();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}