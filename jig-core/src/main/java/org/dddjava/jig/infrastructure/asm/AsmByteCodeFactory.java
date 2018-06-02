package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.bytecode.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Component
public class AsmByteCodeFactory implements org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeFactory {

    final ByteCodeAnalyzeContext byteCodeAnalyzeContext;

    public AsmByteCodeFactory(ByteCodeAnalyzeContext byteCodeAnalyzeContext) {
        this.byteCodeAnalyzeContext = byteCodeAnalyzeContext;
    }

    @Override
    public ByteCodes readFrom(ByteCodeSources byteCodeSources) {
        List<ByteCode> list = new ArrayList<>();
        for (ByteCodeSource source : byteCodeSources.list()) {
            ByteCode byteCode = analyze(source);
            list.add(byteCode);
        }
        return new ByteCodes(list);
    }

    ByteCode analyze(ByteCodeSource byteCodeSource) {
        try (InputStream inputStream = Files.newInputStream(byteCodeSource.getPath())) {
            ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer(byteCodeAnalyzeContext);
            return analyzer.analyze(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}