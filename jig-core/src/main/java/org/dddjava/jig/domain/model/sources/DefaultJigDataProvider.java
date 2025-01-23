package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

import java.util.HashMap;
import java.util.Map;

public record DefaultJigDataProvider(TypeFacts typeFacts, Terms terms, Map<Class<?>, Object> map)
        implements JigDataProvider {

    public DefaultJigDataProvider(TypeFacts typeFacts, Terms terms) {
        this(typeFacts, terms, new HashMap<>());
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
        return typeFacts().enumModels();
    }

    @Override
    public JigTypes fetchJigTypes() {
        return typeFacts().jigTypes();
    }

    @Override
    public Terms fetchTerms() {
        return terms;
    }
}
