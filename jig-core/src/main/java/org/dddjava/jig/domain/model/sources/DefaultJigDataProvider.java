package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.sources.jigfactory.ByteSourceModel;
import org.dddjava.jig.domain.model.sources.jigfactory.TextSourceModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public record DefaultJigDataProvider(ByteSourceModel byteSourceModel,
                                     TextSourceModel textSourceModel,
                                     Map<Class<?>, Object> map,
                                     AtomicReference<JigTypes> jigTypesAtomicReference)
        implements JigDataProvider {

    public DefaultJigDataProvider(ByteSourceModel byteSourceModel, TextSourceModel textSourceModel) {
        this(byteSourceModel, textSourceModel, new HashMap<>(), new AtomicReference<>());
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
        return textSourceModel().enumModels();
    }

    @Override
    public JigTypes fetchJigTypes() {
        if (jigTypesAtomicReference().get() == null) {
            JigTypes jigTypes = initJigTypes();
            if (jigTypesAtomicReference().compareAndSet(null, jigTypes)) {
                return jigTypes;
            }
        }
        return jigTypesAtomicReference().get();
    }

    private JigTypes initJigTypes() {
        return byteSourceModel.jigTypeBuilders().stream()
                .map(jigTypeBuilder -> jigTypeBuilder.applyTextSource(textSourceModel).build())
                .collect(Collectors.collectingAndThen(Collectors.toList(), JigTypes::new));
    }

    @Override
    public Terms fetchTerms() {
        return textSourceModel().toTerms();
    }
}
