package jig.domain.model.relation;

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
}
