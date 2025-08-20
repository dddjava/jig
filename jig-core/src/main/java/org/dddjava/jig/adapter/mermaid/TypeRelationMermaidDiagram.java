package org.dddjava.jig.adapter.mermaid;

import org.dddjava.jig.application.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationship;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * 型関連図
 *
 * 指定パッケージ内の関連を出力する。
 * パッケージ外との関連は20個未満の場合のみ出力する。
 */
public class TypeRelationMermaidDiagram {

    public static final String CONTEXT_KEY = "relationships";

    /**
     * @param jigPackage                出力対象パッケージ
     * @param coreTypesAndRelations 出力コンテキスト
     * @return Mermaidテキスト
     */
    public Optional<String> write(JigPackage jigPackage, CoreTypesAndRelations coreTypesAndRelations) {
        PackageId packageId = jigPackage.packageId();
        TypeRelationships typeRelationships = coreTypesAndRelations.internalTypeRelationships();

        Map<Boolean, List<TypeRelationship>> partitioningRelations = typeRelationships.list().stream()
                // fromがこのパッケージを対象とし、このパッケージのクラスから外のクラスへの関連を出力する。
                // toを対象にすると広く使われるクラス（たとえばIDなど）があるパッケージは見れたものではなくなるので出さない。
                .filter(typeRelationship -> typeRelationship.from().packageId().equals(packageId))
                // パッケージ内の関連とパッケージ外の関連を仕分ける
                .collect(partitioningBy(typeRelationship -> typeRelationship.to().packageId().equals(packageId)));
        if (partitioningRelations.get(true).isEmpty()) {
            // パッケージ内の関連がない場合は出力しない
            return Optional.empty();
        }

        // 外部関連を表示する合計関連数の閾値
        int threshold = 20;
        int externalRelationNumber = partitioningRelations.get(false).size();
        boolean omitExternalRelations = externalRelationNumber > 0 && partitioningRelations.get(true).size() + externalRelationNumber > threshold;
        List<TypeRelationship> targetRelationships = omitExternalRelations
                ? partitioningRelations.get(true)
                : partitioningRelations.values().stream().flatMap(Collection::stream).toList();

        // 関連に含まれるnodeをパッケージの内側と外側に仕分け＆ラベル付け
        Set<TypeId> targetTypes = targetRelationships.stream()
                .flatMap(typeRelationship -> Stream.of(typeRelationship.from(), typeRelationship.to()))
                .collect(toSet());
        // 内側:true, 外側:false のMapに振り分ける
        Map<Boolean, List<String>> nodeMap = targetTypes.stream()
                .collect(partitioningBy(typeId -> typeId.packageId().equals(packageId),
                        mapping(typeId -> {
                                    String label = coreTypesAndRelations.coreJigTypes()
                                            .resolveJigType(typeId).map(JigType::label)
                                            .orElseGet(typeId::asSimpleName);
                                    return MermaidSupport.box(mermaidId(typeId), label);
                                },
                                toList())));

        StringJoiner diagramText = new StringJoiner("\n    ", "\ngraph TB\n    ", "");
        if (nodeMap.containsKey(true)) {
            diagramText.add("subgraph %s[\"%s\"]".formatted(mermaidId(jigPackage.packageId()), jigPackage.label()));
            diagramText.add("direction TB");
            nodeMap.get(true).forEach(diagramText::add);
            diagramText.add("end");
        }
        if (nodeMap.containsKey(false)) {
            nodeMap.get(false).forEach(diagramText::add);
        }
        // クリックでジャンプ
        targetTypes.stream().map(id -> "click %s \"#%s\"".formatted(mermaidId(id), id.fqn())).forEach(diagramText::add);

        // 推移簡約して出力
        // （ここで関連数が減るので閾値と一致しなくなっている）
        new TypeRelationships(targetRelationships).toEdges()
                .transitiveReduction()
                .list().stream()
                .map(edge -> "%s --> %s".formatted(mermaidId(edge.from()), mermaidId(edge.to())))
                .forEach(diagramText::add);

        if (omitExternalRelations) {
            diagramText.add("A@{ shape: braces, label: \"関連数が%dを超えるため、外部への関連は省略されました。\" }".formatted(threshold));
        }

        return Optional.of(diagramText.toString());
    }

    /**
     * mermaidのidに使用するテキストへの変換
     *
     * PackageIdで使用できる文字は使用できそうなのと、
     * このダイアグラムでは種類も多くないのでFQNをそのまま使用する。
     */
    private Object mermaidId(PackageId packageId) {
        return packageId.asText();
    }

    /**
     * mermaidのidに使用するテキストへの変換
     *
     * TypeIdで使用できる文字は使用できそうなのと、
     * このダイアグラムでは種類も多くないのでFQNをそのまま使用する。
     */
    private String mermaidId(TypeId typeId) {
        return typeId.fqn();
    }
}
