package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.parts.classes.rdbaccess.Sqls;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;

import java.util.HashMap;
import java.util.Map;

public record JigSource(TypeFacts typeFacts, MutableSource mutableSource) {

    public JigSource(TypeFacts typeFacts) {
        this(typeFacts, new MutableSource(new HashMap<>()));
    }

    public void addSqls(Sqls sqls) {
        mutableSource().map().put(Sqls.class, sqls);
    }

    public Sqls sqls() {
        return (Sqls) mutableSource().map().getOrDefault(Sqls.class, Sqls.empty());
    }

    public EnumModels enumModels() {
        return typeFacts().enumModels();
    }

    private record MutableSource(Map<Class<?>, Object> map) {
    }
}
