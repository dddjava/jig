package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.classsources.*;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Repository
public class AsmClassSourceReader implements ClassSourceReader {
    private static final Logger logger = LoggerFactory.getLogger(AsmClassSourceReader.class);

    @Override
    public ClassSourceModel classSourceModel(ClassSources classSources) {
        List<ClassDeclaration> classDeclarations = classSources.list().stream()
                .map(classSource -> classDeclaration(classSource))
                .flatMap(Optional::stream)
                .toList();
        return new ClassSourceModel(classDeclarations);
    }

    public Optional<ClassDeclaration> classDeclaration(ClassSource classSource) {
        try {
            AsmClassVisitor asmClassVisitor = new AsmClassVisitor();

            ClassReader classReader = new ClassReader(classSource.value());
            classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);

            return Optional.of(new ClassDeclaration(
                    asmClassVisitor.jigTypeBuilder(),
                    asmClassVisitor.jigTypeHeader()
            ));
        } catch (Exception e) {
            logger.warn("クラスの読み取りに失敗しました。スキップして続行します。 {}", classSource, e);
            return Optional.empty();
        }
    }
}