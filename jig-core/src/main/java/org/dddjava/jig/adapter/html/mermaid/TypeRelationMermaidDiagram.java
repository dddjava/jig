package org.dddjava.jig.adapter.html.mermaid;

import org.dddjava.jig.application.JigTypesWithRelationships;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.module.JigPackage;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TypeRelationMermaidDiagram {

    public static final String CONTEXT_KEY = "relationships";

    public Optional<String> write(JigPackage jigPackage, JigTypesWithRelationships jigTypesWithRelationships) {
        PackageIdentifier packageIdentifier = jigPackage.packageIdentifier();
        TypeRelationships typeRelationships = jigTypesWithRelationships.typeRelationships();

        Map<Boolean, List<TypeRelationship>> partitioningRelations = typeRelationships.list().stream()
                // fromがこのパッケージを対象とし、このパッケージのクラスから外のクラスへの関連を出力する。
                // toを対象にすると広く使われるクラス（たとえばIDなど）があるパッケージは見れたものではなくなるので出さない。
                .filter(typeRelationship -> typeRelationship.from().packageIdentifier().equals(packageIdentifier))
                .collect(Collectors.partitioningBy(typeRelationship -> typeRelationship.to().packageIdentifier().equals(packageIdentifier)));
        if (partitioningRelations.get(true).isEmpty()) {
            return Optional.empty();
        }

        // 外部関連を表示する閾値
        int threshold = 20;
        int externalRelationNumber = partitioningRelations.get(false).size();
        boolean omitExternalRelations = externalRelationNumber > 0 && partitioningRelations.get(true).size() + externalRelationNumber > threshold;
        List<TypeRelationship> targetRelationships = omitExternalRelations
                ? partitioningRelations.get(true) // パッケージ外への関連の方が多い場合はパッケージ内のみにする
                : partitioningRelations.values().stream().flatMap(Collection::stream).toList();

        // 関連に含まれるnodeをパッケージの内側と外側に仕分け＆ラベル付け
        Set<TypeIdentifier> targetTypes = targetRelationships.stream()
                .flatMap(typeRelationship -> Stream.of(typeRelationship.from(), typeRelationship.to()))
                .collect(Collectors.toSet());
        Map<Boolean, List<String>> nodeMap = targetTypes.stream()
                .collect(Collectors.partitioningBy(typeIdentifier -> typeIdentifier.packageIdentifier().equals(packageIdentifier),
                        Collectors.mapping(typeIdentifier -> {
                                    String label = jigTypesWithRelationships.jigTypes()
                                            .resolveJigType(typeIdentifier).map(JigType::label)
                                            .orElseGet(typeIdentifier::asSimpleName);
                                    return Mermaid.BOX.of(mermaidId(typeIdentifier), label);
                                },
                                Collectors.toList())));

        StringJoiner diagramText = new StringJoiner("\n    ", "\ngraph TB\n    ", "");
        if (nodeMap.containsKey(true)) {
            diagramText.add("subgraph %s[\"%s\"]".formatted(mermaidId(jigPackage.packageIdentifier()), jigPackage.label()));
            diagramText.add("direction TB");
            nodeMap.get(true).forEach(diagramText::add);
            diagramText.add("end");
        }
        if (nodeMap.containsKey(false)) {
            nodeMap.get(false).forEach(diagramText::add);
        }
        // クリックでジャンプ
        targetTypes.stream().map(id -> "click %s \"#%s\"".formatted(mermaidId(id), id.fullQualifiedName())).forEach(diagramText::add);

        targetRelationships.stream()
                .map(relationship -> "%s --> %s".formatted(mermaidId(relationship.from()), mermaidId(relationship.to())))
                .forEach(diagramText::add);
        if (omitExternalRelations) {
            diagramText.add("A@{ shape: braces, label: \"関連数が%dを超えるため、外部への関連は省略されました。\" }".formatted(threshold));
        }

        return Optional.of(diagramText.toString());
    }

    /**
     * mermaidのidに使用するテキストへの変換
     *
     * PackageIdentifierで使用できる文字は使用できそうなのと、
     * このダイアグラムでは種類も多くないのでFQNをそのまま使用する。
     */
    private Object mermaidId(PackageIdentifier packageIdentifier) {
        return packageIdentifier.asText();
    }

    /**
     * mermaidのidに使用するテキストへの変換
     *
     * TypeIdentifierで使用できる文字は使用できそうなのと、
     * このダイアグラムでは種類も多くないのでFQNをそのまま使用する。
     */
    private String mermaidId(TypeIdentifier typeIdentifier) {
        return typeIdentifier.fullQualifiedName();
    }
}
