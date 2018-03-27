package jig.domain.model.relation;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public Relations applyDepth(Depth depth) {
        if (depth.unlimited()) return this;
        List<Relation> list = this.list.stream()
                .map(relation -> relation.applyDepth(depth))
                .distinct()
                .filter(Relation::notSelfRelation)
                .collect(Collectors.toList());
        return new Relations(list);
    }
}
