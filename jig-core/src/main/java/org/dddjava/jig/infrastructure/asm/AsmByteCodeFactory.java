package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.bytecode.*;
import org.objectweb.asm.ClassReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AsmByteCodeFactory implements ByteCodeFactory {

    final ByteCodeAnalyzeContext byteCodeAnalyzeContext;

    public AsmByteCodeFactory(ByteCodeAnalyzeContext byteCodeAnalyzeContext) {
        this.byteCodeAnalyzeContext = byteCodeAnalyzeContext;
    }

    @Override
    public ByteCodes readFrom(ByteCodeSources byteCodeSources) {
        List<ByteCode> list = new ArrayList<>();
        for (ByteCodeSource source : byteCodeSources.list()) {
            ByteCode byteCode = readSpecification(source);
            list.add(byteCode);
        }
        return new ByteCodes(list);
    }

    ByteCode readSpecification(ByteCodeSource byteCodeSource) {
        try (InputStream inputStream = Files.newInputStream(byteCodeSource.getPath())) {
            SpecificationReadingVisitor visitor = new SpecificationReadingVisitor(byteCodeAnalyzeContext);
            ClassReader classReader = new ClassReader(inputStream);
            classReader.accept(visitor, ClassReader.SKIP_DEBUG);

            return visitor.specification();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}