package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSource;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeSources;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class AsmByteCodeFactory implements org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeFactory {

    @Override
    public TypeByteCodes readFrom(ByteCodeSources byteCodeSources) {
        List<TypeByteCode> list = new ArrayList<>();
        for (ByteCodeSource source : byteCodeSources.list()) {
            TypeByteCode typeByteCode = analyze(source);
            list.add(typeByteCode);
        }
        return new TypeByteCodes(list);
    }

    TypeByteCode analyze(ByteCodeSource byteCodeSource) {
        try (InputStream inputStream = Files.newInputStream(byteCodeSource.getPath())) {
            ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer();
            return analyzer.analyze(inputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}