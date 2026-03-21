package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * 出力アダプタ群
 */
public record OutboundAdapters(Collection<OutboundAdapter> values) {

    public static OutboundAdapters from(JigTypes jigTypes, ExternalAccessorRepositories accessorRepositories) {
        return jigTypes.orderedStream()
                .filter(jigType -> jigType.typeCategory() == TypeCategory.OutboundAdapter)
                .map(jigType -> OutboundAdapter.from(jigType, jigTypes, accessorRepositories))
                .collect(collectingAndThen(toUnmodifiableList(), OutboundAdapters::new));
    }

    public Stream<OutboundAdapter> stream() {
        return values.stream();
    }
}
