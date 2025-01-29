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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public record DefaultJigDataProvider(ClassSourceModel classSourceModel,
                                     JavaSourceModel javaSourceModel,
                                     Map<Class<?>, Object> map,
                                     AtomicReference<JigTypes> jigTypesAtomicReference)
        implements JigDataProvider {

    public DefaultJigDataProvider(ClassSourceModel classSourceModel, JavaSourceModel javaSourceModel) {
        this(classSourceModel, javaSourceModel, new HashMap<>(), new AtomicReference<>());
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
        return jigTypesAtomicReference().get();
    }

    @Override
    public Terms fetchTerms() {
        return javaSourceModel().toTerms();
    }

    public void initialize() {
        if (jigTypesAtomicReference().get() == null) {
            JigTypes jigTypes = classSourceModel.jigTypeBuilders().stream()
                    .map(jigTypeBuilder -> jigTypeBuilder.applyTextSource(javaSourceModel).build())
                    .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
            jigTypesAtomicReference().compareAndSet(null, jigTypes);
        }
    }
}
