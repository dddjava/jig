package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import java.util.Comparator;
import java.util.List;
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
}
