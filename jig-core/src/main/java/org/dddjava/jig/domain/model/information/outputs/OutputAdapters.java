package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * 出力アダプタ群
 */
public record OutputAdapters(Collection<OutputAdapter> values) {

    public static OutputAdapters from(JigTypes jigTypes) {
        return jigTypes.orderedStream()
                .filter(jigType -> jigType.typeCategory() == TypeCategory.OutputAdapter)
                .map(OutputAdapter::new)
                .collect(collectingAndThen(toUnmodifiableList(), OutputAdapters::new));
    }

    public Stream<OutputAdapter> stream() {
        return values.stream();
    }
}
