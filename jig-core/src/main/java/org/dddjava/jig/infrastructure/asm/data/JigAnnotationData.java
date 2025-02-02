package org.dddjava.jig.infrastructure.asm.data;

import java.util.Collection;

public record JigAnnotationData(JigObjectId<JigTypeData> id,
                                Collection<JigAnnotationElementValueData> elementValueData) {
}
