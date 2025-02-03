package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.JigTypeBuilder;
import org.dddjava.jig.domain.model.sources.classsources.ClassSource;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceReader;
import org.dddjava.jig.domain.model.sources.classsources.ClassSources;
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
        List<JigTypeBuilder> jigTypeBuilders = classSources.list().stream()
                .map(classSource -> typeByteCode(classSource))
                .flatMap(Optional::stream)
                .toList();
        return new ClassSourceModel(jigTypeBuilders);
    }

    Optional<JigTypeBuilder> typeByteCode(ClassSource classSource) {
        try {
            AsmClassVisitor asmClassVisitor = new AsmClassVisitor();

            ClassReader classReader = new ClassReader(classSource.value());
            classReader.accept(asmClassVisitor, ClassReader.SKIP_DEBUG);

            return Optional.of(asmClassVisitor.jigTypeBuilder());
        } catch (Exception e) {
            logger.warn("クラスの読み取りに失敗しました。スキップして続行します。 {}", classSource, e);
            return Optional.empty();
        }
    }
}