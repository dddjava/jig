package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.rdbaccess.MyBatisStatements;
import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

import java.util.HashMap;
import java.util.Map;

public record JigSource(TypeFacts typeFacts, Terms terms, Map<Class<?>, Object> map) {

    public JigSource(TypeFacts typeFacts, Terms terms) {
        this(typeFacts, terms, new HashMap<>());
    }

    public void addSqls(MyBatisStatements myBatisStatements) {
        map().put(MyBatisStatements.class, myBatisStatements);
    }

    public MyBatisStatements sqls() {
        return (MyBatisStatements) map().getOrDefault(MyBatisStatements.class, MyBatisStatements.empty());
    }

    public EnumModels enumModels() {
        return typeFacts().enumModels();
    }
}
