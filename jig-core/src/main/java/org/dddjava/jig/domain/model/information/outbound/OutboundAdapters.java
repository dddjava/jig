package org.dddjava.jig.domain.model.information.outbound;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 出力アダプタ群
 */
public record OutboundAdapters(Collection<OutboundAdapter> values,
                               Map<JigMethodId, OutboundPortOperation> outboundPortOperationMap) {

    public static OutboundAdapters from(JigTypes jigTypes, ExternalAccessorRepositories accessorRepositories) {
        List<OutboundAdapter> outboundAdapters = jigTypes.orderedStream()
                .filter(jigType -> jigType.typeCategory() == TypeCategory.OutboundAdapter)
                .map(jigType -> OutboundAdapter.from(jigType, jigTypes, accessorRepositories))
                .toList();

        // 実装しているPortOperationを収集してMethodIdで引けるようにしておく
        // Portは不要なのかは気になっている
        Map<JigMethodId, OutboundPortOperation> outboundPortOperationMap = outboundAdapters.stream()
                .flatMap(adapter -> adapter.outboundPortStream().flatMap(OutboundPort::operationStream))
                .collect(Collectors.toMap(OutboundPortOperation::jigMethodId, Function.identity(), (existing, replacement) -> existing));

        return new OutboundAdapters(outboundAdapters, outboundPortOperationMap);
    }

    public Optional<OutboundPortOperation> findPortOperation(JigMethodId jigMethodId) {
        return Optional.ofNullable(outboundPortOperationMap.get(jigMethodId));
    }

    public Stream<OutboundAdapter> stream() {
        return values.stream();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}
