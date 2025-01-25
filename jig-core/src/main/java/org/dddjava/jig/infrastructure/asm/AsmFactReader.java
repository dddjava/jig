package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.annotation.Repository;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSource;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.jigfactory.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.JigTypeBuilder;
import org.dddjava.jig.domain.model.sources.jigreader.FactReader;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AsmFactReader implements FactReader {
    private static final Logger logger = LoggerFactory.getLogger(AsmFactReader.class);

    @Override
    public ClassSourceModel byteSourceModel(ClassSources classSources) {
        return classSources.list().stream()
                .map(classSource -> typeByteCode(classSource))
                .flatMap(Optional::stream)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> ClassSourceModel.from(list)));
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