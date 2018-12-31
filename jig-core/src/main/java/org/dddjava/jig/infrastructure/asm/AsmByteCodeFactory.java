package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.bytecode.*;
import org.dddjava.jig.domain.model.implementation.raw.ClassSource;
import org.dddjava.jig.domain.model.implementation.raw.ClassSources;

import java.util.ArrayList;
import java.util.List;

public class AsmByteCodeFactory implements ByteCodeFactory {

    @Override
    public TypeByteCodes readFrom(ClassSources classSources) {
        List<TypeByteCode> list = new ArrayList<>();
        for (ClassSource source : classSources.list()) {
            TypeByteCode typeByteCode = analyze(source);
            list.add(typeByteCode);
        }
        return new TypeByteCodes(list);
    }

    TypeByteCode analyze(ClassSource classSource) {
        ByteCodeAnalyzer analyzer = new ByteCodeAnalyzer();
        return analyzer.analyze(classSource);
    }
}