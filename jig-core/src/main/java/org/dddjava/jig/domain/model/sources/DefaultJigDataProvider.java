package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.information.JigDataProvider;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

import java.util.stream.Collectors;

public record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                                     JigTypes jigTypes,
                                     MyBatisStatements myBatisStatements,
                                     Glossary glossary) implements JigDataProvider {

    public static DefaultJigDataProvider from(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel, MyBatisStatements myBatisStatements, GlossaryRepository glossaryRepository) {
        return new DefaultJigDataProvider(javaSourceModel, initializeJigTypes(classSourceModel, glossaryRepository), myBatisStatements, glossaryRepository.all());
    }

    @Override
    public MyBatisStatements fetchMybatisStatements() {
        return myBatisStatements;
    }

    @Override
    public EnumModels fetchEnumModels() {
        return javaSourceModel().enumModels();
    }

    @Override
    public JigTypes fetchJigTypes() {
        return jigTypes();
    }

    @Override
    public Glossary fetchGlossary() {
        return glossary;
    }

    private static JigTypes initializeJigTypes(ClassSourceModel classSourceModel, GlossaryRepository glossaryRepository) {
        return classSourceModel.classDeclarations()
                .stream()
                .map(classDeclaration -> {
                    return JigType.from(
                            classDeclaration.jigTypeHeader(),
                            classDeclaration.jigTypeMembers(glossaryRepository),
                            glossaryRepository.collectJigTypeTerms(classDeclaration.jigTypeHeader().id())
                    );
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

}
