package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 出力アダプタ群
 */
public record OutputAdapters(Collection<OutputAdapter> values) {

    public static OutputAdapters from(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .filter(jigType -> jigType.typeCategory() == TypeCategory.OutputAdapter)
                .map(OutputAdapter::new)
                .collect(Collectors.collectingAndThen(Collectors.toUnmodifiableList(), OutputAdapters::new));
    }

    public Stream<OutputAdapter> stream() {
        return values.stream();
    }
}
