package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.information.JigDataProvider;
import org.dddjava.jig.domain.model.information.type.JigType;
import org.dddjava.jig.domain.model.information.type.JigTypeMembers;
import org.dddjava.jig.domain.model.information.type.JigTypes;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.classsources.JigMemberBuilder;
import org.dddjava.jig.domain.model.sources.classsources.JigMethodBuilder;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

import java.util.stream.Collectors;

public record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                                     JigTypes jigTypes,
                                     MyBatisStatements myBatisStatements,
                                     Glossary glossary) implements JigDataProvider {

    public static DefaultJigDataProvider from(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel, MyBatisStatements myBatisStatements, GlossaryRepository glossaryRepository) {
        return new DefaultJigDataProvider(javaSourceModel, initializeJigTypes(classSourceModel, javaSourceModel, glossaryRepository), myBatisStatements, glossaryRepository.all());
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

    private static JigTypes initializeJigTypes(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel, GlossaryRepository glossaryRepository) {
        return classSourceModel.classDeclarations().stream()
                .map(classDeclaration -> {
                    // メソッドのコメント登録
                    for (JigMethodBuilder jigMethodBuilder : classDeclaration.jigMemberBuilder().allMethodBuilders()) {
                        var term = glossaryRepository.getMethodTermPossiblyMatches(jigMethodBuilder.methodIdentifier());
                        jigMethodBuilder.registerMethodTerm(term);
                    }

                    JigMemberBuilder jigMemberBuilder = classDeclaration.jigMemberBuilder();
                    JigTypeMembers jigTypeMembers = jigMemberBuilder.buildMember();

                    return buildJigType(classDeclaration.jigTypeHeader(), jigTypeMembers, glossaryRepository);
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

    private static JigType buildJigType(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers, GlossaryRepository glossaryRepository) {
        return JigType.from(
                jigTypeHeader,
                jigTypeMembers,
                glossaryRepository.collectJigTypeTerms(jigTypeHeader.id())
        );
    }

}
