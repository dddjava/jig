package jig.domain.model.relation;

import jig.domain.model.thing.Names;

import java.util.List;

public class Relations {

    List<Relation> list;

    public Relations(List<Relation> list) {
        this.list = list;
    }

    public List<Relation> list() {
        return list;
    }

    public Names collectToNames() {
        return list.stream().map(Relation::to).collect(Names.collector());
    }
}
