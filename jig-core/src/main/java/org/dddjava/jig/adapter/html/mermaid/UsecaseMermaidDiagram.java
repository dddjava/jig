package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public record UsecaseMermaidDiagram(
        JigTypes contextJigTypes,
        MethodRelations methodRelations
) {

    public String textFor(JigMethod jigMethod) {
        var mermaidText = new StringJoiner("\n");
        mermaidText.add("graph LR");

        // 基点からの呼び出し全部 + 直近の呼び出し元
        var filteredRelations = methodRelations.filterFromRecursive(jigMethod.jigMethodIdentifier())
                .merge(methodRelations.filterTo(jigMethod.jigMethodIdentifier()));

        // 解決済み（Usecaseメソッドに含まれるもの）を識別するためのコレクション
        // filteredRelationsに問い合わせればいい気もする
        Set<JigMethodIdentifier> resolved = new HashSet<>();

        // メソッドのスタイル
        filteredRelations.jigMethodIdentifierStream().forEach(jigMethodIdentifier -> {
            // 自分は太字にする
            if (jigMethodIdentifier.equals(jigMethod.jigMethodIdentifier())) {
                resolved.add(jigMethodIdentifier);
                mermaidText.add(usecaseMermaidNodeText(jigMethod));
                mermaidText.add("style %s font-weight:bold".formatted(htmlIdText(jigMethod.jigMethodIdentifier())));
            } else {
                contextJigTypes.resolveJigMethod(jigMethodIdentifier)
                        .ifPresent(method -> {
                            resolved.add(jigMethodIdentifier);
                            if (method.remarkable()) {
                                // 出力対象のメソッドはusecase型＆クリックできるように
                                mermaidText.add(usecaseMermaidNodeText(method));
                                var htmlIdText = htmlIdText(method.jigMethodIdentifier());
                                mermaidText.add("click %s \"#%s\"".formatted(htmlIdText, htmlIdText));
                            } else {
                                // remarkableでないものは普通の。privateメソッドなど該当。　
                                mermaidText.add(normalMermaidNodeText(method));
                            }
                        });
            }
        });

        Set<TypeIdentifier> others = new HashSet<>();

        Function<JigMethodIdentifier, Optional<String>> converter = jigMethodIdentifier -> {
            // 解決済みのメソッドは出力済みなので、Mermaid上のIDだけでよい
            if (resolved.contains(jigMethodIdentifier)) {
                return Optional.of(htmlIdText(jigMethodIdentifier));
            }
            // 解決できなかったものは関心が薄いとして、メソッドではなくクラスとして解釈し
            var typeIdentifier = jigMethodIdentifier.tuple().declaringTypeIdentifier();
            if (typeIdentifier.packageIdentifier().equals(jigMethod.declaration().declaringType().packageIdentifier())) {
                // 暫定的に同じパッケージのもののみ出力する
                // Serviceの場合に出力したいのはControllerやRepositoryになるので、気が向いたらなんとかする
                others.add(typeIdentifier);
                return Optional.of(typeIdentifier.htmlIdText());
            } else {
                return Optional.empty();
            }
        };
        mermaidText.add(filteredRelations.mermaidEdgeText(converter));

        // JigMethodにならないものはクラスノードとして出力する
        others.forEach(typeIdentifier ->
                mermaidText.add("%s[%s]:::others".formatted(typeIdentifier.htmlIdText(), typeIdentifier.asSimpleText())));

        mermaidText.add("classDef others fill:#AAA,font-size:90%;");
        mermaidText.add("classDef lambda fill:#999,font-size:80%;");

        return mermaidText.toString();
    }

    private String normalMermaidNodeText(JigMethod jigMethod) {
        var jigMethodIdentifier = jigMethod.jigMethodIdentifier();
        var string = htmlIdText(jigMethodIdentifier);
        if (jigMethodIdentifier.isLambda()) {
            return "%s[\"%s\"]:::lambda".formatted(string, "(lambda)");
        }
        return "%s[\"%s\"]".formatted(string, jigMethod.labelText());
    }

    private String usecaseMermaidNodeText(JigMethod jigMethod) {
        return "%s([\"%s\"])".formatted(htmlIdText(jigMethod.jigMethodIdentifier()), jigMethod.labelTextOrLambda());
    }

    private static String htmlIdText(JigMethodIdentifier jigMethodIdentifier) {
        var tuple = jigMethodIdentifier.tuple();

        var typeText = tuple.declaringTypeIdentifier().packageAbbreviationText();
        var parameterText = tuple.parameterTypeIdentifiers().stream()
                .map(TypeIdentifier::packageAbbreviationText)
                .collect(Collectors.joining(", ", "(", ")"));
        return (typeText + '.' + tuple.name() + parameterText).replaceAll("[^a-zA-Z0-9]", "_");
    }
}
