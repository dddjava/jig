package jig.infrastructure.asm;

import jig.domain.model.specification.*;
import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AsmClassFileReader implements ModelReader {

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