package org.dddjava.jig.domain.model.information.inbound;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public record InboundAdapters(List<InboundAdapter> groups, MethodRelations methodRelations) {

    public static InboundAdapters from(JigTypes jigTypes) {
        return new InboundAdapters(
                jigTypes.orderedStream()
                        .flatMap(jigType -> InboundAdapter.from(jigType).stream())
                        .toList(),
                // TODO 全MethodRelationsを入れているが、EntryPointからのRelationだけあればいいはず
                MethodRelations.from(jigTypes));
    }

    public List<Entrypoint> listEntrypoint() {
        return httpEntrypointStream().toList();
    }

    private Stream<Entrypoint> httpEntrypointStream() {
        return groups.stream()
                .flatMap(inboundAdapter -> inboundAdapter.entrypoints().stream())
                .filter(entrypointMethod -> entrypointMethod.entrypointType() == EntrypointType.HTTP_API);
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public Collection<Entrypoint> collectEntrypointMethodOf(CallerMethods callerMethods) {
        return httpEntrypointStream()
                .filter(entrypointMethod -> {
                    JigMethodId jigMethodId = entrypointMethod.jigMethod().jigMethodId();
                    return callerMethods.values().stream()
                            .anyMatch(item -> item.equals(jigMethodId));
                })
                .toList();
    }
}
