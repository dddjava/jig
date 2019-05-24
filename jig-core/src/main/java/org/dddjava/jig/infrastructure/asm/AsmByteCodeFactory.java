package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.ByteCodeFactory;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.raw.classfile.ClassSource;
import org.dddjava.jig.domain.model.implementation.raw.classfile.ClassSources;

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