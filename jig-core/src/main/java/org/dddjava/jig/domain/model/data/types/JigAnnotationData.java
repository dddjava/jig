package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.List;

public record JigAnnotationData(JigObjectId<JigAnnotationData> id,
                                Collection<JigAnnotationElementValueData> elementValueData) {

    public static JigAnnotationData from(String name) {
        return new JigAnnotationData(new JigObjectId<>(name), List.of());
    }

    public String simpleTypeName() {
        return id.simpleValue();
    }
}
