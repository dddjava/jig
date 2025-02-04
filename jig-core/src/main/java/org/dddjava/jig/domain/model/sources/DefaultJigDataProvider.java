package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.sources.classsources.ClassSourceModel;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record DefaultJigDataProvider(JavaSourceModel javaSourceModel,
                                     Map<Class<?>, Object> map,
                                     JigTypes jigTypes)
        implements JigDataProvider {

    public DefaultJigDataProvider(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel) {
        this(javaSourceModel, new HashMap<>(), initializeJigTypes(classSourceModel, javaSourceModel));
    }

    public void addSqls(MyBatisStatements myBatisStatements) {
        map().put(MyBatisStatements.class, myBatisStatements);
    }

    @Override
    public MyBatisStatements fetchMybatisStatements() {
        return (MyBatisStatements) map().getOrDefault(MyBatisStatements.class, MyBatisStatements.empty());
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
    public Terms fetchTerms() {
        return javaSourceModel().toTerms();
    }

    private static JigTypes initializeJigTypes(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel) {
        return classSourceModel.classDeclarations().stream()
                .map(classDeclaration -> classDeclaration.jigTypeBuilder().applyTextSource(javaSourceModel).build(classDeclaration.jigTypeHeader()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }
}
