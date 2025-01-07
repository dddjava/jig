package org.dddjava.jig.domain.model.models.applications.inputs;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public record Entrypoint(List<EntrypointGroup> list, MethodRelations methodRelations) {

    public static Entrypoint from(JigTypes jigTypes, MethodRelations methodRelations) {
        return new Entrypoint(jigTypes.list().stream()
                .map(jigType -> EntrypointGroup.from(jigType))
                .filter(entrypointGroup -> entrypointGroup.hasEntrypoint())
                .toList(),
                methodRelations);
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

    public List<TypeIdentifier> listTypeIdentifiers() {
        return list.stream()
                .map(entrypointGroup -> entrypointGroup.jigType().identifier())
                .toList();
    }

    public static List<Map.Entry<String, Function<EntrypointMethod, Object>>> reporter() {
        return List.of(
                Map.entry("パッケージ名", item -> item.typeIdentifier().packageIdentifier().asText()),
                Map.entry("クラス名", item -> item.typeIdentifier().asSimpleText()),
                Map.entry("メソッドシグネチャ", item -> item.method().declaration().asSignatureSimpleText()),
                Map.entry("メソッド戻り値の型", item -> item.method().declaration().methodReturn().asSimpleText()),
                Map.entry("クラス別名", item -> item.jigType.typeAlias().asText()),
                Map.entry("使用しているフィールドの型", item -> item.method().usingFields().typeIdentifiers().asSimpleText()),
                Map.entry("分岐数", item -> item.method().decisionNumber().intValue()),
                Map.entry("パス", item -> item.pathText())
        );
    }
}
