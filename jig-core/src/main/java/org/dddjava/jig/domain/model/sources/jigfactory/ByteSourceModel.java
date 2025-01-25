package org.dddjava.jig.domain.model.sources.jigfactory;

import java.util.List;

public record ByteSourceModel(List<JigTypeBuilder> jigTypeBuilders) {

    public static ByteSourceModel from(List<JigTypeBuilder> list) {
        return new ByteSourceModel(list);
    }
}
