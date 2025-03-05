package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record Entrypoints(List<EntrypointGroup> list, MethodRelations methodRelations) {

    public static Entrypoints from(EntrypointMethodDetector entrypointMethodDetector, JigTypes jigTypes) {
        return new Entrypoints(
                jigTypes.stream()
                        .flatMap(jigType -> EntrypointGroup.from(entrypointMethodDetector, jigType).stream())
                        .toList(),
                // TODO 全MethodRelationsを入れているが、EntryPointからのRelationだけあればいいはず
                MethodRelations.from(jigTypes));
    }

    public List<EntrypointMethod> listRequestHandlerMethods() {
        return requetHandlerMethodStream().toList();
    }

    private Stream<EntrypointMethod> requetHandlerMethodStream() {
        return list.stream()
                .flatMap(entrypointGroup -> entrypointGroup.entrypointMethods().stream())
                .filter(entrypointMethod -> entrypointMethod.entrypointType() == EntrypointType.HTTP_API);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public List<EntrypointMethod> collectEntrypointMethodOf(CallerMethods callerMethods) {
        return requetHandlerMethodStream()
                .filter(entrypointMethod -> entrypointMethod.anyMatch(callerMethods))
                .toList();
    }

    public JigTypes jigTypes() {
        return list().stream().map(EntrypointGroup::jigType).collect(collectingAndThen(toList(), JigTypes::new));
    }
}
