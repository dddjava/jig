package org.dddjava.jig.domain.model.implementation.relation;

import java.util.List;

public class MethodRelations {

    List<MethodRelation> list;

    public MethodRelations(List<MethodRelation> list) {
        this.list = list;
    }

    public MethodRelationStream stream() {
        return new MethodRelationStream(list.stream());
    }
}
