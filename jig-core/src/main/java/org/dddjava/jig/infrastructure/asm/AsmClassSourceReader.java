package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.classsources.ClassSource;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

@Repository
public class AsmClassSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(AsmClassSourceReader.class);

    public Collection<ClassDeclaration> readClasses(ClassSources classSources) {
        return classSources.values().stream()
                .map(classSource -> classDeclaration(classSource))
                .flatMap(Optional::stream)
                .toList();
    }

    public Optional<ClassDeclaration> classDeclaration(ClassSource classSource) {
        try {
            AsmClassVisitor asmClassVisitor = new AsmClassVisitor();

            ClassReader classReader = new ClassReader(classSource.bytes());
            classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);

            return Optional.of(asmClassVisitor.classDeclaration());
        } catch (Exception e) {
            logger.warn("クラスの読み取りに失敗しました。スキップして続行します。 {}", classSource, e);
            return Optional.empty();
        }
    }
}