package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.jigsource.file.binary.ClassSource;
import org.dddjava.jig.domain.model.jigsource.file.binary.ClassSources;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.FactFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.objectweb.asm.ClassReader;

import java.util.ArrayList;
import java.util.List;

public class AsmFactFactory implements FactFactory {

    @Override
    public TypeFacts readTypeFacts(ClassSources classSources) {
        List<TypeFact> list = new ArrayList<>();
        for (ClassSource source : classSources.list()) {
            TypeFact typeFact = typeByteCode(source);
            list.add(typeFact);
        }
        return new TypeFacts(list);
    }

    TypeFact typeByteCode(ClassSource classSource) {
        AsmClassVisitor asmClassVisitor = new AsmClassVisitor();
        ClassReader classReader = new ClassReader(classSource.value());
        classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);
        return asmClassVisitor.typeFact;
    }
}