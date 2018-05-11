package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.*;
import org.dddjava.jig.domain.model.implementation.ImplementationSource;
import org.dddjava.jig.domain.model.implementation.ImplementationSources;
import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AsmImplementationFactory implements ImplementationFactory {

    final ImplementationAnalyzeContext implementationAnalyzeContext;

    public AsmImplementationFactory(ImplementationAnalyzeContext implementationAnalyzeContext) {
        this.implementationAnalyzeContext = implementationAnalyzeContext;
    }

    @Override
    public Implementations readFrom(ImplementationSources implementationSources) {
        List<Implementation> list = new ArrayList<>();
        for (ImplementationSource source : implementationSources.list()) {
            Implementation implementation = readSpecification(source);
            list.add(implementation);
        }
        return new Implementations(list);
    }

    Implementation readSpecification(ImplementationSource implementationSource) {
        try (InputStream inputStream = Files.newInputStream(implementationSource.getPath())) {
            SpecificationReadingVisitor visitor = new SpecificationReadingVisitor(implementationAnalyzeContext);
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            return visitor.specification();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}