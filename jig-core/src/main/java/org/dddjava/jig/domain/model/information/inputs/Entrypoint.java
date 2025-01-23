package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public record Entrypoint(List<EntrypointGroup> list, MethodRelations methodRelations) {

    public static Entrypoint from(JigTypes jigTypes) {
        return new Entrypoint(
                jigTypes.stream()
                        .flatMap(jigType -> EntrypointGroup.from(jigType).stream())
                        .toList(),
                // TODO 全MethodRelationsを入れているが、EntryPointからのRelationだけあればいいはず
                jigTypes.methodRelations());
    }

    public Map<String, String> mermaidMap(JigTypes jigTypes) {
        var map = new HashMap<String, String>();

        for (EntrypointGroup entrypointGroup : list()) {
            var jigType = entrypointGroup.jigType();
            var mermaidText = entrypointGroup.mermaid(methodRelations, jigTypes);
            map.put(jigType.fqn(), mermaidText);
        }

        return map;
    }

    public List<EntrypointMethod> listRequestHandlerMethods() {
        return requetHandlerMethodStream().toList();
    }

    private Stream<EntrypointMethod> requetHandlerMethodStream() {
        return list.stream()
                .filter(entrypointGroup -> entrypointGroup.isRequestHandler())
                .flatMap(entrypointGroup -> entrypointGroup.entrypointMethod().stream());
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
