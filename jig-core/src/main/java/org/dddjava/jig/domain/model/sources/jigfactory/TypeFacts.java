package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.data.enums.EnumModels;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {

    private final List<JigTypeBuilder> list;
    private final EnumModels enumModels;

    public TypeFacts(List<JigTypeBuilder> list, EnumModels enumModels) {
        this.list = list;
        this.enumModels = enumModels;
    }

    private JigTypes jigTypes;

    public JigTypes jigTypes() {
        if (jigTypes != null) return jigTypes;
        jigTypes = new JigTypes(list.stream().map(JigTypeBuilder::build).collect(toList()));
        return jigTypes;
    }

    public EnumModels enumModels() {
        return enumModels;
    }

}
