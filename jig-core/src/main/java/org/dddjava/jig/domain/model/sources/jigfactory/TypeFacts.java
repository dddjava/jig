package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {

    private final List<JigTypeBuilder> list;

    public TypeFacts(List<JigTypeBuilder> list) {
        this.list = list;
    }

    private JigTypes jigTypes;

    public JigTypes jigTypes() {
        if (jigTypes != null) return jigTypes;
        jigTypes = new JigTypes(list.stream().map(JigTypeBuilder::build).collect(toList()));
        return jigTypes;
    }
}
