package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record InputAdapters(List<InputAdapter> groups, MethodRelations methodRelations) {

    public static InputAdapters from(EntrypointMethodDetector entrypointMethodDetector, JigTypes jigTypes) {
        return new InputAdapters(
                jigTypes.orderedStream()
                        .flatMap(jigType -> InputAdapter.from(entrypointMethodDetector, jigType).stream())
                        .toList(),
                // TODO 全MethodRelationsを入れているが、EntryPointからのRelationだけあればいいはず
                MethodRelations.from(jigTypes));
    }

    public List<Entrypoint> listEntrypoint() {
        return entrypointStream().toList();
    }

    private Stream<Entrypoint> entrypointStream() {
        return groups.stream()
                .flatMap(inputAdapter -> inputAdapter.entrypoints().stream())
                .filter(entrypointMethod -> entrypointMethod.entrypointType() == EntrypointType.HTTP_API);
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public Collection<Entrypoint> collectEntrypointMethodOf(CallerMethods callerMethods) {
        return entrypointStream()
                .filter(entrypointMethod -> entrypointMethod.anyMatch(callerMethods))
                .toList();
    }

    public JigTypes jigTypes() {
        return groups().stream().map(InputAdapter::jigType).collect(collectingAndThen(toList(), JigTypes::new));
    }
}
