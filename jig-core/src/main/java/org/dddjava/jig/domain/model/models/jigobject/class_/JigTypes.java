package org.dddjava.jig.domain.model.models.jigobject.class_;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JigTypes {

    List<JigType> list;

    public JigTypes(List<JigType> list) {
        this.list = list;
    }

    public List<JigType> listCollectionType() {
        return list.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.コレクション)
                .sorted(Comparator.comparing(JigType::identifier))
                .collect(Collectors.toList());
    }

    public List<JigType> listCategoryType() {
        return list.stream()
                .filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分)
                .sorted(Comparator.comparing(JigType::identifier))
                .collect(Collectors.toList());
    }

    public List<JigType> list() {
        return list;
    }

    public List<JigType> listMatches(Predicate<JigType> predicate) {
        return list.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }
}
