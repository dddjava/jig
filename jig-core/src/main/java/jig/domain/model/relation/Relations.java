package jig.domain.model.relation;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.MethodIdentifier;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

    public List<GenericRelation<Identifier, MethodIdentifier>> list2() {
        return list().stream()
                .map(relation -> new GenericRelation<>(relation.from(), new MethodIdentifier(relation.to())))
                .collect(toList());
    }
}
