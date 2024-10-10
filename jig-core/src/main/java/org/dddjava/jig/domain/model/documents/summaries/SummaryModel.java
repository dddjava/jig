package org.dddjava.jig.domain.model.documents.summaries;

import org.dddjava.jig.domain.model.models.applications.entrypoints.Entrypoint;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryType;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class SummaryModel {
    Map<PackageIdentifier, List<JigType>> map;
    // FIXME enumModelsの持ち方・・・
    EnumModels enumModels;
    Map<String, String> mermaidMap;

    private SummaryModel(Map<PackageIdentifier, List<JigType>> map, EnumModels enumModels) {
        this.map = map;
        this.enumModels = enumModels;
    }

    public static SummaryModel from(BusinessRules businessRules) {
        Map<PackageIdentifier, List<JigType>> map = businessRules.list().stream()
                .map(BusinessRule::jigType)
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(map, new EnumModels(List.of()));
    }

    public static SummaryModel from(JigTypes jigTypes) {
        Map<PackageIdentifier, List<JigType>> map = jigTypes.list().stream()
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(map, new EnumModels(List.of()));
    }

    public static SummaryModel from(CategoryTypes categoryTypes, EnumModels enumModels) {
        Map<PackageIdentifier, List<JigType>> map = categoryTypes.list().stream()
                .map(CategoryType::jigType)
                .collect(groupingBy(JigType::packageIdentifier));
        return new SummaryModel(map, enumModels);
    }

    public static SummaryModel from(Entrypoint entrypoint) {
        Map<PackageIdentifier, List<JigType>> map = entrypoint.list().stream()
                .map(entrypointGroup -> entrypointGroup.jigType())
                .collect(groupingBy(JigType::packageIdentifier));

        var summaryModel = new SummaryModel(map, new EnumModels(List.of()));
        summaryModel.mermaidMap = entrypoint.mermaidMap();
        return summaryModel;
    }

    public Map<PackageIdentifier, List<JigType>> map() {
        return map;
    }

    public boolean empty() {
        return map.isEmpty();
    }

    public EnumModels enumModels() {
        return enumModels;
    }

    /**
     * htmlから使用
     */
    public Map<String, String> mermaidMap() {
        if (mermaidMap != null) return mermaidMap;

        // 対象となっているJigTypes
        var serviceJigTypes = map().values().stream()
                .flatMap(List::stream)
                .collect(collectingAndThen(toList(), JigTypes::new));

        // 認識範囲のmethodの関連
        var methodRelations = serviceJigTypes.list().stream()
                .flatMap(jigType -> jigType.methodStream())
                // メソッドの関連に変換
                .flatMap(jigMethod -> jigMethod.methodInstructions().stream()
                        .filter(toMethod -> !toMethod.isJSL()) // JSLを除く
                        .filter(toMethod -> !toMethod.isConstructor()) // コンストラクタ呼び出しを除く
                        .map(toMethod -> new MethodRelation(jigMethod.declaration(), toMethod)))
                .collect(collectingAndThen(toList(), MethodRelations::new));

        // 対象のメソッド単位に処理
        var collect = serviceJigTypes.list().stream()
                .flatMap(jigType -> jigType.instanceMethods().list().stream())
                // 基点になるものだけ対象
                .filter(JigMethod::remarkable)
                .collect(toMap(
                        jigMethod -> jigMethod.fqn(),
                        jigMethod -> jigMethod.usecaseMermaidText(serviceJigTypes, methodRelations)
                ));
        return collect;
    }
}
