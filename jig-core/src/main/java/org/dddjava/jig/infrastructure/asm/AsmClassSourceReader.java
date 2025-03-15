package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.classsources.ClassFile;
import org.dddjava.jig.domain.model.sources.classsources.ClassFiles;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

@Repository
public class AsmClassSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(AsmClassSourceReader.class);

    public Collection<ClassDeclaration> readClasses(ClassFiles classFiles) {
        return classFiles.values().stream()
                .map(classFile -> classDeclaration(classFile))
                .flatMap(Optional::stream)
                .toList();
    }

    public Optional<ClassDeclaration> classDeclaration(ClassFile classFile) {
        try {
            AsmClassVisitor asmClassVisitor = new AsmClassVisitor();

            ClassReader classReader = new ClassReader(classFile.bytes());
            classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);

            return Optional.of(asmClassVisitor.classDeclaration());
        } catch (Exception e) {
            logger.warn("クラスの読み取りに失敗しました。スキップして続行します。 {}", classFile, e);
            return Optional.empty();
        }
    }
}