package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.sources.file.binary.ClassSource;
import org.dddjava.jig.domain.model.sources.file.binary.ClassSources;
import org.dddjava.jig.domain.model.sources.jigfactory.JigTypeBuilder;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.sources.jigreader.FactReader;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AsmFactReader implements FactReader {
    private static final Logger logger = LoggerFactory.getLogger(AsmFactReader.class);

    @Override
    public TypeFacts readTypeFacts(ClassSources classSources, TextSourceModel textSourceModel) {
        return classSources.list().stream()
                .map(classSource -> typeByteCode(classSource))
                .flatMap(Optional::stream)
                .map(jigTypeBuilder -> jigTypeBuilder.applyTextSource(textSourceModel))
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> new TypeFacts(list, textSourceModel.enumModels())));
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