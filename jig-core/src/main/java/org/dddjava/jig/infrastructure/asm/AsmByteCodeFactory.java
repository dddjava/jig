package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.bytecode.*;

import java.util.ArrayList;
import java.util.List;

public class AsmByteCodeFactory implements ByteCodeFactory {

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
        ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer();
        return analyzer.analyze(byteCodeSource);
    }
}