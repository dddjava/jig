package org.dddjava.jig.infrastructure.asm.data;

import java.util.Collection;
import java.util.List;

public record JigAnnotationData(JigObjectId<JigTypeData> id,
                                Collection<JigAnnotationElementValueData> elementValueData) {

    public static JigAnnotationData from(String name) {
        return new JigAnnotationData(new JigObjectId<>(name), List.of());
    }

    public String simpleTypeName() {
        return id.simpleValue();
    }
}
