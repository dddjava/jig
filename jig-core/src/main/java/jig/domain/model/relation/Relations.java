package jig.domain.model.relation;

import jig.domain.model.thing.Names;

import java.util.Comparator;
import java.util.List;

public class Relations {

    List<Relation> list;

    public Relations(List<Relation> list) {
        this.list = list;
        // クラス名昇順、メソッド名昇順
        list.sort(Comparator.<Relation, String>comparing(relation -> relation.from().value())
                .thenComparing(relation -> relation.to().value()));
    }

    public List<Relation> list() {
        return list;
    }

    public Names collectToNames() {
        return list.stream().map(Relation::to).collect(Names.collector());
    }
}
