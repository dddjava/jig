package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.application.GlossaryRepository;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.JigInstanceMember;
import org.dddjava.jig.domain.model.data.classes.type.JigStaticMember;
import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

import java.util.stream.Collectors;

public record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                                     JigTypes jigTypes,
                                     MyBatisStatements myBatisStatements)
        implements JigDataProvider {

    public static DefaultJigDataProvider from(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel, MyBatisStatements myBatisStatements, GlossaryRepository glossaryRepository) {
        return new DefaultJigDataProvider(javaSourceModel, initializeJigTypes(classSourceModel, javaSourceModel, glossaryRepository), myBatisStatements);
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
    public Glossary fetchTerms() {
        return javaSourceModel().toTerms();
    }

    private static JigTypes initializeJigTypes(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel, GlossaryRepository glossaryRepository) {
        return classSourceModel.classDeclarations().stream()
                .map(classDeclaration -> {
                    // クラスのコメント
                    javaSourceModel.optClassComment(classDeclaration.jigTypeHeader().id())
                            .ifPresent(classComment -> classDeclaration.jigMemberBuilder().registerClassComment(classComment));
                    // メソッドのコメント登録
                    for (JigMethodBuilder jigMethodBuilder : classDeclaration.jigMemberBuilder().allMethodFacts()) {
                        javaSourceModel.methodImplementations.stream()
                                .filter(methodImplementation -> methodImplementation.possiblyMatches(jigMethodBuilder.methodIdentifier()))
                                .findAny()
                                .ifPresent(methodImplementation -> jigMethodBuilder.registerMethodImplementation(methodImplementation));
                    }

                    JigMemberBuilder jigMemberBuilder = classDeclaration.jigMemberBuilder();
                    JigStaticMember jigStaticMember = jigMemberBuilder.buildStaticMember();
                    JigInstanceMember jigInstanceMember = jigMemberBuilder.buildInstanceMember();

                    return buildJigType(classDeclaration.jigTypeHeader(), jigStaticMember, jigInstanceMember, glossaryRepository);
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

    private static JigType buildJigType(JigTypeHeader jigTypeHeader, JigStaticMember jigStaticMember, JigInstanceMember jigInstanceMember, GlossaryRepository glossaryRepository) {
        return JigType.from(
                jigTypeHeader,
                jigStaticMember,
                jigInstanceMember,
                glossaryRepository.collectJigTypeTerms(jigTypeHeader.id())
        );
    }

}
